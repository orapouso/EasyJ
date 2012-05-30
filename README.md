EasyJ Library
========================================================
A collection of small simple libraries to simplify some tasks when dealing with JEE application.
It deals with JPA, Bean Validation, Spring, Http Client and so on.

The library is divided in four small libs:

 - EasyJ Http - Creates a wrapper around HttpClient for easier chaining http requests
 - EasyJ Spring - Some small spring view classes for JSON parsing
 - EasyJ ORM - A generic DAO/Service solution for ORM with JPA
 - EasyJ REST - A @Controller class structure to deal with common REST tasks to CRUD entities


Dependencies:

 Each library has its set o dependency but generally they depend on the libraries bellow

 - Spring Web MVC 3.1.*
 - Commons Logging 1.1.*
 - JSON-Lib 2.3
 - Http Client 4.1.*
 - Commons Http Client 3.1
 - Hibernate Validator 4.1.*
 - Hibernate JPA 3.6.*