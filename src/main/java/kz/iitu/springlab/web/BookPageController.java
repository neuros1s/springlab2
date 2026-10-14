package kz.iitu.springlab.web;

import java.util.Map;
import kz.iitu.springlab.catalog.Book;
import kz.iitu.springlab.catalog.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/books")
public class BookPageController {
    private final BookService service;

    public BookPageController(BookService service) {
        this.service = service;
    }

    @GetMapping
    public String list(@RequestParam(value = "author", required = false) String author,
                       Model model) {
        model.addAttribute("books", service.findAll(author));
        model.addAttribute("author", author);
        model.addAttribute("title", "Catalogue");
        return "books/list";
    }

    @PostMapping(consumes = "application/x-www-form-urlencoded")
    public String create(@RequestParam("title") String title,
                         @RequestParam("author") String author,
                         @RequestParam("year") int year,
                         RedirectAttributes redirect) {
        Book saved = service.create(new Book(null, title, author, year));
        redirect.addFlashAttribute("message", "Book \"" + saved.title() + "\" has been added");
        return "redirect:/books";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, String> badInput(IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage());
    }
}
