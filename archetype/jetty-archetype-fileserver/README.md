#Using the archetype

##Create your project
Go to a directory where you'd like to create your project.

Generate from the archetype:
    $ mvn archetype:generate -DarchetypeGroupId=org.mortbay.jetty.archetype -DarchetypeArtifactId=jetty-archetype-fileserver

Follow the prompts and choose your values for groupId, artifactId, version, and base package

It will also ask you for the version of jetty you wish to use.

Your project will be created in a directory named for your artifact id. 

##Running Locally

Go to your project directory.

Build the application:

    $ mvn install

Run the application:

    $ java -jar artifactId.jar

Now you can test your application by opening a browser and going to http://localhost:8080
