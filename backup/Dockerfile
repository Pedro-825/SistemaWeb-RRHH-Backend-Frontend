FROM postgres:16-alpine

RUN apk add --no-cache dcron

COPY backup.sh /backup.sh
RUN chmod +x /backup.sh

# Cron: todos los domingos a las 02:00 AM
RUN echo "0 2 * * 0 /backup.sh >> /var/log/backup.log 2>&1" > /etc/crontabs/root

RUN mkdir -p /backups

CMD ["crond", "-f", "-l", "2"]
