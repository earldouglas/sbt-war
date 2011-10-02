This is an attempt to rethink plugin architecture, clean out some mess and introduce flexibility and separation of concerns.

# Changes summary

1. Existing plugin has been broken down into two separate facilities: container and webapp. Compound plugin is still available for simplicity
2. Webapp is running directly from sources without intermediate exploded war assembly
3. Breakdown into two parts allows to implement deployment of multiple projects into one web server




