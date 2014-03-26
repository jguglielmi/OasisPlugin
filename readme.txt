OasisPlugin

This is a plugin for Fitnesse to allow extra automation functionality directly in the wiki edit page. Read below for more information about OASIS.


OASIS
(Open Automated Source Integrated Suite)

OASIS is an open source, Java based, and platform independent FitNesse plugin that allows testers and developers the ability to automate tests using a variety of different tools quickly and with ease. Built around the Xebium framework, OASIS seeks to increase the capabilities of testing by adding in thick client and load testing support.

Using the various tools built into the framework, a user can quickly construct an automated test without much training and with no coding background necessary. The ultimate goal of OASIS is to increase collaboration between testing and development, while also preserving the relevancy of a test case to business owners, product owners, and other analysts in an agile environment.

OASIS pulls from the open source tools:

-Sikuli
-Synthuse
-Xebium (FitNesse + Selenium)
-JMeter

run "mvn install" to generate the file with dependencies within the target directory and when you're ready to update your Maven project, add it in.

To generate the ivy plugin, run the command "ant resolve dist" from command line in the base directory.

For examples of how to use OASIS, please visit the wiki page and Youtube videos. The plugin offers support for the FitNesse Maven Classpath plugin.

To find out how to set it up further, follow the instructions here:

https://github.com/amolenaar/fitnesse-maven-classpath

or

view the live examples at
https://github.com/jguglielmi/OASIS