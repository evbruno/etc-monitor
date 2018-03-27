var app1 = new Vue({
  el: '#serverComponent',
  created: function() {

    var self = this;

    var reload = function() {
        axios.get("http://localhost:9999/api/v2").then(response => {
          self.servers = response.data;
        });
    };
    reload();
    setInterval(reload, 10000);
  },
  data:
    function () {
        return  {
            servers: []
        }
  },
  methods: {}
});