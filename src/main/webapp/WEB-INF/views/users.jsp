<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.store.model.User" %>
<%@ page import="java.util.List" %>
<%
    List<User> users = (List<User>) request.getAttribute("users");
%>
<!DOCTYPE html>
<html lang="uk">
<head>
    <meta charset="UTF-8">
    <title>Список користувачів</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>./css/styles.css">
</head>
<body>
<h1>Список користувачів</h1>
<table>
    <tr>
        <th>ID</th>
        <th>Ім'я</th>
        <th>Email</th>
        <th>Пароль</th>
        <th>Роль</th>
    </tr>
    <% for(User u : users) { %>
    <tr>
        <td><%= u.getId() %></td>
        <td><%= u.getName() %></td>
        <td><%= u.getEmail() %></td>
        <td><%= u.getPassword() %></td>
        <td><%= u.getRole() %></td>
    </tr>
    <% } %>
</table>
</body>
</html>

