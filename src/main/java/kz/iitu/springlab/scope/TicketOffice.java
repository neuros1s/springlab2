package kz.iitu.springlab.scope;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import java.util.Map;
@Service
public class TicketOffice {
    private final Ticket fixed;
    private final ObjectProvider<Ticket> tickets;
    public TicketOffice(Ticket fixed, ObjectProvider<Ticket> tickets) {
        this.fixed = fixed; this.tickets = tickets;
    }
    public Map<String,String> demonstrate() {
        return Map.of("injectedPrototype", fixed.id(), "providerFirst", tickets.getObject().id(),
            "providerSecond", tickets.getObject().id(), "singletonOffice", Integer.toHexString(System.identityHashCode(this)));
    }
}
