package com.gpig.a.tickets;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;

import com.gpig.a.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 */
public class TicketContent {

    /**
     * An array of items.
     */
    public static final List<Ticket> ITEMS = new ArrayList<>();

    /**
     * A map of items, by ID.
     */
    public static final Map<String, Ticket> ITEM_MAP = new HashMap<>();

    private static final int COUNT = 5;

    static {
        addItem(new Ticket(String.valueOf(1), "Booking Code Sample", "Booking Ref: 19S5P4ZTVP"));
        addItem(new Ticket(String.valueOf(2), "QR Sample", "", R.drawable.qr_test));
        // TODO Add some real tickets.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createTicket(i+2));
        }
    }

    private static void addItem(Ticket item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static Ticket createTicket(int position) {
        return createTicket(position, -1);
    }

    private static Ticket createTicket(int position, int image) {
        return new Ticket(String.valueOf(position), "Sample Ticket " + position, makeDetails(position), image);
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Ticket: ").append(position);
        return builder.toString();
    }
}
