Vue.component('disk-item', {
    props: ['obj'],
    template: '#disk-item-template'
});

Vue.component('mem-item', {
    props: ['obj'],
    template: '#mem-item-template'
});

Vue.component('result-template', {
    props: ['obj'],
    template: '#result-template'
});


const __sharedData = {
    cols: 2,
    refreshRate: 120,
    refreshRateServer: 10,
    showMonitorTop: true,
    servers: [],
    url: 'http://127.0.0.1:7788/api'
}


var monitorCfg = new Vue({
    el: '#monitorCfg',
    created: function () {
        var loadCol = localStorage.getItem('cols')
        this.updateCols(parseInt(loadCol || 2));

        // var self = this;
        // var url = 'http://' + document.location.host + ':' + document.location.port + '/api';
        // var url = 'http://127.0.0.1:7788/api';
        //
        // axios.get(url + '/servers').then(response => {
        //     self.servers = response.data.servers;
        // });
    },
    data: {
        sharedData: __sharedData
    },
    methods: {
        colStyle: function () {
            return 'col-md-' + (12 / this.sharedData.cols);
        },
        updateCols: function (n) {
            localStorage.setItem('cols', n);
            this.sharedData.cols = n;
        },
        updateShowMonitorTop: function (n) {
            //localStorage.setItem('cols', n);
            //this.sharedData.cols = n;
            console.log(n);
        }
    },
    watch: {
        'sharedData.cols': function (val, oldVal) {
            localStorage.setItem('cols', val);
        },
        'sharedData.refreshRate': function (val, oldVal) {
            localStorage.setItem('refreshRate', val);
        }
    }
});

var app1 = new Vue({
    el: '#serverComponent',
    created: function () {
        var self = this;
        //var url = 'http://' + document.location.host + ':' + document.location.port + '/api';
        //var url = 'http://127.0.0.1:7788/api';

        axios.get(self.sharedData.url + '/servers').then(response => {
            self.sharedData.servers = response.data.servers;
            self.sharedData.refreshRate = response.data.refreshRate;
        });
    },
    data:
        function () {
            return {
                //servers: [],
                cols: 2,
                sharedData: __sharedData,
                intervals: {},
                count: -1
            }
        },
    methods: {
        colStyle: function () {
            //return 'col-md-' + (12 / this.cols);
            return 'col-md-' + (12 / this.sharedData.cols);
        }
    },
    watch: {
        'sharedData.servers': function (val, oldVal) {
            var self = this;

            // var reload = function(a) {
            //     return function() {
            //         console.log('refreshing ' + a);
            //     }
            // };
            //var w = new Worker("my_worker.js");
            // w.onmessage = function(event){
            //     //document.getElementById("result").innerHTML = event.data;
            //     console.log(event.data);
            //     self.count = event.data;
            // };
            //
            // setTimeout(function () {
            //     w.postMessage("hi there");
            // }, 20000);
            //
            // setTimeout(function () {
            //     w.terminate();
            // }, 60000);

            // FIXME clear old intervals ?
            this.intervals = {};

            if (val && val.length) {
                for(var i = 0; i < val.length; i ++) {
                    var server = val[i];

                    var taskFun = function (responseAlias) {
                        return function() {
                            axios.get(self.sharedData.url + '/tasks/' + responseAlias).then(response => {
                                console.log('response for ' + responseAlias + '>>' + response);
                                var idx = self.sharedData.servers.findIndex(s => s.alias === responseAlias);
                                self.sharedData.servers[idx].tasks = response.data;
                            });
                        };
                    };

                    var alias = server.alias;

                    taskFun(alias).call();

                    this.intervals[server.alias] = setInterval(taskFun(alias), self.sharedData.refreshRate * 1000);
                }
            }
        }
    }
});

