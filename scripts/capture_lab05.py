from pathlib import Path
from datetime import datetime, timezone
import argparse, json, subprocess, time, urllib.request, urllib.error
import xml.etree.ElementTree as ET
from playwright.sync_api import sync_playwright

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'evidence' / 'lab05'

def require(condition, message):
    if not condition:
        raise AssertionError(message)

def header(response, name):
    return next((v for k, v in response['headers'].items()
                 if k.lower() == name.lower()), '')

def main(port=8080):
    OUT.mkdir(parents=True, exist_ok=True)
    base = f'http://127.0.0.1:{port}'
    observations = {}
    browser_requests = []
    tests = {'tests': 0, 'failures': 0, 'errors': 0, 'skipped': 0}
    for fp in (ROOT / 'target/surefire-reports').glob('TEST-*.xml'):
        suite = ET.parse(fp).getroot()
        for key in tests:
            tests[key] += int(suite.attrib.get(key, 0))
    require(tests['tests'] > 0 and all(tests[x] == 0 for x in ('failures','errors','skipped')),
            'Maven tests must pass before evidence capture')

    def call(name, method, path, expected=200, payload=None, headers=None):
        request_headers = dict(headers or {})
        data = None
        if payload is not None:
            data = payload if isinstance(payload, bytes) else json.dumps(payload).encode('utf-8')
        req = urllib.request.Request(base + path, data=data, headers=request_headers, method=method)
        try:
            response = urllib.request.urlopen(req, timeout=10)
        except urllib.error.HTTPError as ex:
            response = ex
        with response:
            row = {'method': method, 'path': path, 'requestHeaders': request_headers,
                   'requestBody': data.decode('utf-8') if data is not None else '',
                   'status': response.status, 'headers': dict(response.headers.items()),
                   'body': response.read().decode('utf-8')}
        observations[name] = row
        require(row['status'] == expected, f'{name}: expected {expected}, got {row["status"]}')
        print(f'{method} {path} -> {row["status"]}', flush=True)
        return row

    log = (OUT / 'server.log').open('w')
    jar = ROOT / 'target/spring-lab-01-0.0.1-SNAPSHOT.jar'
    process = subprocess.Popen(['java','-jar',str(jar),f'--server.port={port}'],
                               cwd=ROOT, stdout=log, stderr=subprocess.STDOUT)
    try:
        started = False
        for _ in range(180):
            if process.poll() is not None:
                raise RuntimeError('The application stopped during startup; inspect server.log')
            try:
                with urllib.request.urlopen(base + '/actuator/health', timeout=1) as response:
                    if response.status == 200:
                        started = True
                        break
            except (urllib.error.URLError, TimeoutError):
                pass
            time.sleep(.25)
        require(started, 'Application startup timed out')
        home = call('home', 'GET', '/')
        require('href="/books"' in home['body'], 'Static welcome page link is missing')
        css = call('stylesheet','GET','/css/app.css')
        require('border-collapse' in css['body'], 'Stylesheet was not served')
        require('text/css' in header(css, 'Content-Type'), 'Wrong stylesheet content type')

        with sync_playwright() as p:
            browser = p.chromium.launch()
            context = browser.new_context(viewport={'width': 1000, 'height': 840},
                                          device_scale_factor=1)
            page = context.new_page()
            page.on('response', lambda r: browser_requests.append({
                'method': r.request.method, 'url': r.url, 'status': r.status}))
            response = page.goto(base + '/books', wait_until='networkidle')
            require(response.status == 200, 'Catalogue page failed')
            require(page.locator('tbody tr').count() == 3, 'Expected three seed books')
            require(page.locator('table').evaluate(
                '(table) => getComputedStyle(table).borderCollapse') == 'collapse',
                'Catalogue CSS was not applied')
            page.screenshot(path=str(OUT / 'catalogue.png'), full_page=True)
            page.get_by_label('Filter by author').fill('Bloch')
            page.get_by_role('button', name='Apply filter', exact=True).click()
            page.wait_for_url('**/books?author=Bloch')
            require(page.locator('tbody tr').count() == 1, 'Author filter failed')
            require('Joshua Bloch' in page.locator('tbody').inner_text(), 'Wrong filtered author')
            page.screenshot(path=str(OUT / 'filtered.png'), full_page=True)
            page.goto(base + '/books', wait_until='networkidle')
            before = page.locator('tbody tr').count()
            page.get_by_label('Title', exact=True).fill('Java Precisely')
            page.get_by_label('Author', exact=True).fill('Sestoft')
            page.get_by_label('Year', exact=True).fill('2016')
            with page.expect_response(lambda r: r.request.method == 'POST'
                                      and r.url == base + '/books') as post_info:
                page.get_by_role('button', name='Add book', exact=True).click()
            post = post_info.value
            require(post.status == 302, 'Browser form did not redirect')
            require(post.headers.get('location', '').endswith('/books'),
                    'Wrong redirect target')
            page.wait_for_url(base + '/books')
            page.get_by_role('status').wait_for(state='visible')
            require('Java Precisely' in page.get_by_role('status').inner_text(),
                    'Flash message is missing')
            redirected_get = next(r for r in reversed(browser_requests)
                                  if r['method'] == 'GET' and r['url'] == base + '/books')
            require(redirected_get['status'] == 200, 'Redirected GET failed')
            after = page.locator('tbody tr').count()
            require(after == before + 1, 'The form must create exactly one book')
            page.screenshot(path=str(OUT / 'flash.png'), full_page=True)
            reload_response = page.reload(wait_until='networkidle')
            refreshed = page.locator('tbody tr').count()
            require(reload_response.status == 200, 'Refresh failed')
            require(refreshed == after, 'Refresh created a duplicate book')
            require(page.get_by_role('status').count() == 0, 'Flash message survived a refresh')
            page.screenshot(path=str(OUT / 'refreshed.png'), full_page=True)
            prg = {'postStatus': post.status, 'postLocation': post.headers.get('location'),
                   'redirectedGetStatus': redirected_get['status'], 'refreshStatus': reload_response.status,
                   'booksBefore': before, 'booksAfterPost': after,
                   'booksAfterRefresh': refreshed, 'flashRemovedOnRefresh': True}
            browser.close()

        all_books = call('list_all', 'GET', '/api/books')
        require(len(json.loads(all_books['body'])) == 4, 'Expected four books after the form')
        filtered = call('list_filtered','GET','/api/books?author=Bloch&limit=1')
        require(len(json.loads(filtered['body'])) == 1, 'REST query filter or limit failed')
        call('read_one','GET','/api/books/1')
        call('read_absent','GET','/api/books/999',404)
        new_book = {'title':'Pro Spring 6','author':'Cosmina','year':2023}
        created = call('create','POST','/api/books',201,new_book,
                       {'Content-Type':'application/json'})
        new_id = json.loads(created['body'])['id']
        require(header(created,'Location') == f'/api/books/{new_id}', 'Wrong creation Location')
        call('read_created','GET',f'/api/books/{new_id}')
        replacement = {'id':999,'title':'Effective Java Updated','author':'Joshua Bloch','year':2019}
        replaced = call('replace','PUT','/api/books/1',200,replacement,
                        {'Content-Type':'application/json'})
        require(json.loads(replaced['body'])['id'] == 1, 'PUT did not use the path id')
        call('replace_absent','PUT','/api/books/999',404,replacement,
             {'Content-Type':'application/json'})
        call('put_absent_confirm','GET','/api/books/999',404)
        removed = call('delete_first','DELETE','/api/books/2',204)
        require(removed['body'] == '', '204 must have an empty body')
        call('delete_second','DELETE','/api/books/2',404)
        call('error_400','GET','/api/books/abc',400)
        call('error_404','GET','/api/nothing',404)
        call('error_405','DELETE','/api/books',405)
        call('error_415','POST','/api/books',415,b'Not JSON',{'Content-Type':'text/plain'})
        call('error_406','GET','/api/books',406,headers={'Accept':'application/xml'})
        call('bad_limit','GET','/api/books?limit=abc',400)
        call('negative_limit','GET','/api/books?limit=-1',400)
        en = call('variant_en','GET','/api/books?author=Bloch&limit=1',
                  headers={'Accept-Language':'en'})
        ru = call('variant_ru','GET','/api/books?author=Bloch&limit=1',
                  headers={'Accept-Language':'ru'})
        en_data, ru_data = json.loads(en['body'])[0], json.loads(ru['body'])[0]
        require(set(en_data) == {'id','title','author','year'}, 'English field names failed')
        require(set(ru_data) == {'id','название','автор','год'}, 'Russian field names failed')
        require(en_data['title'] == ru_data['название'], 'Language changed the stored values')
        require(header(ru,'Content-Language') == 'ru', 'Wrong Russian language header')
        require('Accept-Language' in header(ru,'Vary'), 'Missing cache variation header')
        weighted = call('variant_weighted','GET','/api/books?limit=1',
                        headers={'Accept-Language':'en;q=0.2,ru-RU;q=0.9'})
        require(header(weighted,'Content-Language') == 'ru', 'Language priorities were ignored')
        fallback = call('variant_fallback','GET','/api/books?limit=1',
                        headers={'Accept-Language':'kk'})
        require(header(fallback,'Content-Language') == 'en', 'Unsupported language fallback failed')
        java_version = subprocess.check_output(['java','-version'],stderr=subprocess.STDOUT,
                                              text=True).strip()
        result = {'checksPassed':True, 'capturedAt':datetime.now(timezone.utc).isoformat(),
                  'source':'Live HTTP requests and Chromium screenshots from a running Spring Boot application',
                  'baseUrl':base, 'javaVersion':java_version, 'mavenTests':tests,
                  'requests':observations, 'prg':prg, 'browserRequests':browser_requests}
        (OUT / 'results.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),
                                         encoding='utf-8')
        (OUT / 'browser-requests.json').write_text(json.dumps(browser_requests,indent=2))
        for name,row in observations.items():
            parts=[f'{row["method"]} {row["path"]}',f'HTTP {row["status"]}']
            parts.extend(f'{k}: {v}' for k,v in row['headers'].items())
            parts.extend(['',row['body']])
            (OUT / f'{name}.http.txt').write_text('\n'.join(parts),encoding='utf-8')
        print('All live HTTP, browser, PRG and language-header checks passed', flush=True)
    finally:
        process.terminate()
        try:
            process.wait(timeout=10)
        except subprocess.TimeoutExpired:
            process.kill()
            process.wait(timeout=5)
        log.close()

if __name__ == '__main__':
    parser=argparse.ArgumentParser()
    parser.add_argument('--port',type=int,default=8080)
    main(parser.parse_args().port)
