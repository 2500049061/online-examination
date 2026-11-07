# Online Examination

A small demonstration online examination website using HTML, CSS, JavaScript, and a Java HTTP server.

## Requirements
- JDK 11 or later
- A modern web browser

## Run on Windows (PowerShell or Command Prompt)
1. Extract the ZIP.
2. Open a terminal in the extracted `Online_Examination` folder.
3. Compile:
   ```bash
   javac -d out java/ExamServer.java
   ```
4. Start the server:
   ```bash
   java -cp out ExamServer
   ```
5. Open http://localhost:8080

If port 8080 is already in use, start on another port:
```bash
java -cp out ExamServer 8081
```
Then open http://localhost:8081.

## Demo login
- Username: `student`
- Password: `1234`

## Included features
- Student login (demo credentials)
- Multiple-choice exam with five sample questions
- Countdown timer
- Submit button and automatic submission when time expires
- Score and percentage calculation
- Responsive layout

## Important
This is an educational demonstration. It uses in-memory demo credentials and questions, not a production authentication system or database. The browser sends selected answers to the Java server for scoring.
