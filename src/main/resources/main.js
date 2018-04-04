var _DATA0 = {
       'kind' : 'disk',
       'fileSystem' : '/dev/sda3' ,
       'used' : 100 ,
       'total' : 70 ,
       'available' : 30 ,
       'percentage' : 30
};
var _DATA1 = {
       'kind' : 'disk',
       'fileSystem' : '/dev/sda4' ,
       'used' : 100 ,
       'total' : 2000 ,
       'available' : 1900 ,
       'percentage' : 20
};
var _DATA2 = {
       'kind' : 'mem',
       'used' : 7500 ,
       'total' : 10000 ,
       'free' : 1500
};

Vue.component('disk-item', {
  props: ['obj'],
  template: '#disk-item-template'
});

Vue.component('mem-item', {
  props: ['obj'],
  template: '#mem-item-template'
});


const __sharedData = {
    cols: 2,
    refreshRate : 120,
    showMonitorTop : true
}


var monitorCfg = new Vue({
  el: '#monitorCfg',
  created: function() {
    var loadCol = localStorage.getItem('cols')
    this.updateCols(parseInt( loadCol || 2 ));
  },
  data: {
    sharedData: __sharedData
  },
  methods: {
     colStyle: function() {
        return 'col-md-' + (12 / this.sharedData.cols);
    },
    updateCols: function(n) {
         localStorage.setItem('cols', n);
         this.sharedData.cols = n;
    },
    updateShowMonitorTop: function(n) {
         //localStorage.setItem('cols', n);
         //this.sharedData.cols = n;
         console.log(n);
    }
  },
  watch: {
    'sharedData.cols': function(val, oldVal) {
        localStorage.setItem('cols', val);
    },
    'sharedData.refreshRate': function(val, oldVal) {
        localStorage.setItem('refreshRate', val);
    }
  }
});


var app1 = new Vue({
  el: '#serverComponent',
  created: function() {

    var self = this;

    var reload = function() {
          axios.get("http://10.42.12.136:7777/api/v2").then(response => {
          self.servers = response.data;
          if (self.servers && self.servers.length > 0) {
          	self.servers[1].tasks = [ _DATA0, _DATA1, _DATA2 ];
          }
        });
    };
    //reload();
    //setInterval(reload, 1000 * 120);
    this.servers = [
      {
        alias: 'localhost',
        status: 'online',
        tasks: [ _DATA0, _DATA1, _DATA2 ]
      },
      {
        alias: 'localhost-0',
        status: 'offline',
        tasks: [ ]
      },
      {
        alias: 'localhost-1',
        status: 'offline',
        tasks: [ ]
      },
      {
        alias: 'localhost-2',
        status: 'offline',
        tasks: [ ]
      },
    ];

  },
  data:
    function () {
        return  {
            servers: [],
            cols: 2,
            sharedData: __sharedData
        }
  },
  methods: {
     colStyle: function() {
        //return 'col-md-' + (12 / this.cols);
        return 'col-md-' + (12 / this.sharedData.cols);
    }
  }
});

