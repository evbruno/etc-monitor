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
        });
    };
    reload();
    setInterval(reload, 10000);
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