# java-web-crawler

JCrawlGUI is a Java application with a graphical user interface (GUI) that allows users to scrape URLs, download images, and search for text within saved content files. The application uses JSoup for web scraping and Swing for the GUI components.
<br>
## Features
1. Scrape URLs: Extract URLs from a given web page and save their content to text files. 
2. Download Images: Download all images from a given web page to a specified directory. 
3. Search: Search for a specific term in the saved content files. 
<br>
## Requirements
- Java Development Kit (JDK) 8 or higher
- JSoup library (version 1.17.2)
<br>
## Setup
Clone the repository:
```bash
git clone https://github.com/SrirakshaMS/java-web-crawler.git
```
```bash
cd JCrawlGUI
```
## Compilation and Execution
**Compile the Java file:**
```bash
javac -cp .;jsoup-1.17.2.jar JCrawlGUI.java
```
**Run the application:**
```bash
java -cp .;jsoup-1.17.2.jar JCrawlGUI
```
