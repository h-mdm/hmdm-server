#!/bin/bash
#
# CPU monitoring for Headwind MDM
#
MAILS="info@example.com"
TMP_BODY="/tmp/error_mail.txt"
ERROR_FLAG_FILE="/tmp/hmdm.error_flag"
ERROR_LOG_FILE="/opt/hmdm/cpu_error.log"
CRITICAL_CPU_USAGE=200
LAST_RESTART_FILE="/tmp/hmdm.last_restart"
FIX_SCRIPT="/usr/sbin/service tomcat9 restart"

BC_PATH=$(which bc)
if [ -z "$BC_PATH" ]; then
    apt install bc
fi

TOMCAT_PID=$(ps ax | grep java | grep var/lib/tomcat | awk '{print $1}')
if [ -z "$TOMCAT_PID" ]; then
    # Tomcat not running!
	exit 1
fi

CPU_TOTAL=0

INTERVAL=5
for i in $(seq 1 $INTERVAL); do
    CPU=$(top -b -n 1 -p $TOMCAT_PID | tail -1 | awk '{print $9}')
        if [ -z "$CPU" ]; then
            # Something is wrong
                exit 1
        fi
    CPU_TOTAL=$(echo $CPU_TOTAL + $CPU | bc)
    sleep 1
done

CPU_TOTAL=$(echo "$CPU_TOTAL / $INTERVAL" | bc)

CPU_EXCEEDS=$(echo "$CPU_TOTAL > $CRITICAL_CPU_USAGE" | bc)
if [ $CPU_EXCEEDS -eq 1 ]; then
    # Abnormal CPU usage!!!
	DATETIME=$(date)
    echo "$DATETIME Abnormal CPU usage: $CPU_TOTAL" >> $ERROR_LOG_FILE

     # Prepare e-mail
    echo "Headwind MDM CPU abnormal usage detected: $CPU_TOTAL" > $TMP_BODY
	
	for MAIL in $MAILS
    do
        cat $TMP_BODY | mail -s "Problem at Headwind MDM server" $MAIL
    done
    rm $TMP_BODY

    echo "1" > $ERROR_FLAG_FILE
	
	# Run a fix script
    LAST_RESTART_TIME=$(cat $LAST_RESTART_FILE)
    CURRENT_TIME=$(date +"%s")
    if [ -z "LAST_RESTART_TIME" ]
    then
        $FIX_SCRIPT &
        echo $CURRENT_TIME > $LAST_RESTART_FILE
    else
        NEXT_RESTART_TIME=$(($LAST_RESTART_TIME + 3600))
        if [ $NEXT_RESTART_TIME -lt $CURRENT_TIME ]
        then
            $FIX_SCRIPT &
            echo $CURRENT_TIME > $LAST_RESTART_FILE
        fi
    fi
else
# Check if there was a recent error, then it's recovered
    if [ -f "$ERROR_FLAG_FILE" ] && [ "$(cat $ERROR_FLAG_FILE)" -eq "1" ]
    then
        DATETIME=$(date)
        echo "$DATETIME server recovered" >> $ERROR_LOG_FILE

        # Prepare e-mail
        echo "Headwind MDM server is recovered, CPU usage is normal: $CPU_TOTAL" > $TMP_BODY

        for MAIL in $MAILS
        do
            cat $TMP_BODY | mail -s "Headwind MDM server is recovered" $MAIL
        done
        rm $TMP_BODY

        echo "0" > $ERROR_FLAG_FILE
    fi
fi

