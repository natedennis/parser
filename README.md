# mvn clean compile package install
# java -cp "target/lib/*:target/*" com.natedennis.Parser --duration=hourly --startDate="2017-01-02.13:00:00" --threshold=100 --file=/home/nathandennis/Downloads/access.log



mysql> select  a.ip, count(a.id) from access_log a where a.access_date >= '2017-01-01.13:00' and a.access_date < '2017-01-01.14:00' group by a.ip HAVING COUNT(a.ip)>100;


insert into access_log_filtered_copy select  b.* from access_log b inner join access_log a on b.ip=a.ip where a.access_date >= '2017-01-01.13:00' and a.access_date < '2017-01-01.14:00' group by a.ip HAVING COUNT(a.ip)>50;
