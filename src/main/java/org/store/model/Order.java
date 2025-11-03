package org.store.model;

import java.util.Date;
import java.util.List;

public class Order {
    private int id;
    private int userId;
    private double totalPrice;
    private Date date;
    private String status;
    private List<OrderItem> items;
}