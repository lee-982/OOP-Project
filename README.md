# Chama Member Registry (Java Swing + MySQL)

## Problem
Many chamas (savings groups) in Kenya keep records in notebooks or WhatsApp. This causes lost
records, duplicate members, and disputes over contributions. This system stores and manages
member details in a MySQL database so the treasurer/secretary can add, view, update and delete
members quickly and accurately.

## Setup
1. Install MySQL and run `schema.sql` (MySQL Workbench or `mysql -u root -p < schema.sql`).
2. Download the MySQL Connector/J jar (e.g. mysql-connector-j-8.x.jar) and add it to your project's
   classpath (IDE: Add External JAR / Libraries).
3. Set your MySQL password in `DBConnection.java`.
4. Run `Main.java`.

Command line:
    javac -cp .:mysql-connector-j-8.x.jar *.java
    java  -cp .:mysql-connector-j-8.x.jar Main        (use ; instead of : on Windows)
