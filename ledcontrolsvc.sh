#!/bin/sh

LIB_PATH="./lib"
GW_PATH="ledcontrolsvc"
GW_JAR="ledcontrolsvc-1.0.jar"
GW_NAME="ledcontrolsvc"
GW_MAINCLASS="com.github.uugan.Main"
GW_TAG="ledcontrolsvc"

cd ${GW_PATH}

GW_RUNNING=`ps -ef | grep ${GW_NAME} | grep java`

start()
{
	echo "Starting ${GW_NAME}"
	THE_CLASSPATH=${GW_JAR}
	for i in `ls ${LIB_PATH}/*.jar`
	do
  		THE_CLASSPATH=${i}:${THE_CLASSPATH}
	done

	java -cp "${THE_CLASSPATH}" -Duser.timezone="+8.00" -Dserver.port=8080 -Dled.ip=192.168.88.199 -Dled.port=5005 ${GW_MAINCLASS} &
}

force()
{
	if [ ${#GW_RUNNING} -gt 0 ]; then
		kill -9 `ps -ef | grep ${GW_JAR} | grep java | awk '{print $2}'` 
		echo "stopped!"
	else
		echo "${GW_NAME} is not running"
	fi
}

stop()
{
	if [ ${#GW_RUNNING} -gt 0 ]; then
		kill `ps -ef | grep ${GW_JAR} | grep java | awk '{print $2}'` 
		echo "stopped!"
	else
		echo "${GW_NAME} is not running"
	fi
}

status()
{
	if [ ${#GW_RUNNING} -gt 0 ]; then
		echo "${GW_NAME} is running"
	else
		echo "${GW_NAME} is not running"
	fi
}

if [ "$1" = "start" ] && [ ${#GW_RUNNING} -gt 0 ]; then
	echo "${GW_NAME} is already running"
	exit 0
fi

case $1 in
start)
	start
    ;;
stop)
	stop
    ;;
force)
	force
	;;
restart)
	stop
	start
    ;;
status)
	status
	;;
*)
	echo "Usage: $0 {start|stop|restart|status}" 
	;;
esac
