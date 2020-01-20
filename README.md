# Led Control Service
ledcontrolsvc is a java rapidoid web server for ONBON led display controlling
## Installation
mvn install
copy lib folder with main jar file then run as follow:
./ledcontrolsvc.sh start
./ledcontrolsvc.sh stop
./ledcontrolsvc.sh force

## Example
Send json to http://server_ip:8080/ port :
type \in  { text, dynamictext }

```json
{
  "type":"text",
  "txt":"sample"
}

```
```json
{
"code":"000", 
"msg":"Success"
}
```
When code <> 000 means occurred error.


