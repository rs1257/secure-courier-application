package com.gpig.a.tickets;

public class Ticket {
    public final String id;
    public final String title;
    public final String details;
    public final int image;

    public Ticket(String id, String content, String details) {
        this(id, content, details, -1);
    }

    public Ticket(String id, String content, String details, int draw) {
        this.id = id;
        this.title = content;
        this.details = details;
        this.image = draw;
    }

    @Override
    public String toString() {
        return title;
    }
}
