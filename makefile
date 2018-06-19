build:
	mvn package

travis-deploy:
	mvn clean deploy -P release --settings /path/to/settings.xml