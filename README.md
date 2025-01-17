# WebServerDatabaseApi
- Test case files are present in the installation folder

## Installation
- [Install link](https://daniellinda.net/App.zip)
- unzip
- config should be already present in the installation folder
- if config does not exist then create one and configure

## Configure application.properties
- example configuration:
  - ``log.file.path=[path]``
  - ``database.url=jdbc:mysql://[address]/[schema]``
  - ``database.user=[username]``
  - ``database.password=[password]``

## Startup
- navigate to installation directory
- ``java -jar App.jar [config file path]``
- default config location: ``java -jar App.jar application.properties``

## Sources
- [JavaRush](https://javarush.com/en/groups/posts/en.1896.java-annotations-what-is-it-and-how-to-use-it)
- [JavaTPoint](https://www.javatpoint.com/java-annotation)
- [MartinFowler](https://martinfowler.com/eaaCatalog/dataMapper.html)