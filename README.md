# Smart Lead Qualification

Smart Lead Qualification is a Spring Boot REST API that receives customer or prospect messages, uses an AI model through Hugging Face to determine whether each message is a sales lead, and stores qualified leads in a PostgreSQL database.

The application is designed to help teams automatically identify valuable inbound messages such as demo requests, pricing inquiries, partnerships, support needs, and other business opportunities.

## Features

- Submit inbound messages through a REST API
- Automatically classify whether a message is a sales lead
- Generate lead details using an AI model:
    - Lead title
    - Lead type
    - Urgency level
    - Description
- Store messages and qualified leads in PostgreSQL
- Retrieve recent messages
- Retrieve recent qualified leads
- Delete messages
- Bean validation for required fields
- Spring Data JPA persistence
- Hugging Face chat completions integration

## Tech Stack

- Java 17
- Spring Boot 3.5.14
- Spring Web
- Spring Data JPA
- Spring Validation
- PostgreSQL
- Hugging Face API
- Docker
- Maven
- JUnit 5
- Mockito

## Project Structure
