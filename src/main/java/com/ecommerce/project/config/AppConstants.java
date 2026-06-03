package com.ecommerce.project.config;

/*
* The main reason for creating the AppConstants class is to have a centralized place
  for all default values and constants used across the application.
* For example, parameters like pageNumber, pageSize, sortBy, and sortDir are commonly
  used in many controllers for pagination.
-> By keeping these defaults in one file, we follow the DRY principle (Don’t Repeat Yourself).
  If we ever need to change the default page size from 50 to 20,
  we only modify it in AppConstants instead of updating the same value in every controller method.
-> This improves maintainability, reduces the chance of mistakes,
   and makes the codebase cleaner and more scalable as the project grows with more controllers.
*/
public class AppConstants {

    public static final String PAGE_NUMBER = "0";
    public static final String PAGE_SIZE = "10";
    public static final String SORT_CATEGORIES_BY = "categoryId";
    public static final String SORT_DIR = "asc";

}
