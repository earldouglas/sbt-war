1. Existing plugin has been broken down into two separate facilities: container and webapp. Compound plugin is still available for simplicity
1. Webapp is running directly from sources without intermediate exploded war assembly
1. Breakdown into two parts allows to implement deployment of multiple projects into one web server
1. And also multiple web servers in one project
1. Web server runner interface is now little more abstract. This would allow to implement Tomcat support someday
