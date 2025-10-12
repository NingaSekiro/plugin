package org.aopbuddy.plugin.other;

class User {
    final int id;
    final String name, email;

    User(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
}