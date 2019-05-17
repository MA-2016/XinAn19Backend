# Knowledge

## Filebeat

### Core concepts

#### Harvesters

Reference: https://www.elastic.co/guide/en/beats/filebeat/current/how-filebeat-works.html

* A harvester is responsible for reading the content of a single file, it reads each line and sends the content to the output.
* A harvester is responsible for opening and closing the file, the file descriptor remains open while the harvester is running.
* If a file is removed or renamed while its harvester is running, Filebeat continues to read the file.

#### Inputs

