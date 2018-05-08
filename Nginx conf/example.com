server {
    listen 80;
    #listen [::]:80;
    server_name example.com www.example.com;

    location / {
        root /home/maria/www/example.com/html;
        index index.html index.htm;
        try_files $uri $uri/ =404;
    }

    location ^~ /metrics {
        content_by_lua_block {
            local client = require "resty.kafka.client"
            local producer = require "resty.kafka.producer"
            local cjson = require "cjson"

            local broker_list1 = {
                { host = "10.0.3.9", port = 9092 },
            }

            local broker_list2 = {
                { host = "10.0.3.8", port = 9092 },
            }

            local broker_list3 = {
                { host = "10.0.3.10", port = 9092 },
            }

            local time  = ngx.req.start_time();
            local device = ngx.var.arg_device;
            local metrics = ngx.var.arg_metrics;
            local key = "key";
            local value = cjson.encode({ id = device, value = metrics, ip = ngx.var.remote_addr, time = time  });
            local message = "id="..ngx.var.arg_device..",value="..ngx.var.arg_metrics..",time="..time..",ip="..ngx.var.remote_addr;
            ngx.say(message);

            local cli = client:new(broker_list1)

             local brokers, partitions = cli:fetch_metadata("spark_app")
                if not brokers then
                    ngx.say("fetch_metadata failed, err:", partitions)
                end
                ngx.say("brokers: ", cjson.encode(brokers), "; partitions: ", cjson.encode(partitions))

            local rbrokers, rpartitions = cli:refresh();
                if not brokers then
                    ngx.say("refresh failed, err:", rpartitions)
                end
                ngx.say("brokers: ", cjson.encode(rbrokers), "; partitions: ", cjson.encode(rpartitions))

            local bp1 = producer:new(broker_list1, { producer_type = "async" })
            local bp2 = producer:new(broker_list2, { producer_type = "async" })
            local bp3 = producer:new(broker_list3, { producer_type = "async" })

            local producer_list = {}
            producer_list[0] = bp1
            producer_list[1] = bp2
            producer_list[2] = bp3
            local time = os.date("%S")
            local index = math.mod(time,3)
            local ok, err = producer_list[index]:send("spark_app", key, message)
                if not ok then
                    ngx.say("send err:", err)
                    return
                end
                ngx.say("send success, ok:", ok)
        }
    }

error_page 500 502 503 504 /50x.html;
   location = /50x.html {
     # root html;
      root  /usr/share/nginx/html;
   }
}

                               
