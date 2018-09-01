module.exports = {
    initialize: function(merchantId, sandbox, success) {
        cordova.exec(
			success,
			null,
            'ZarinPalPlugin',
            'initialize',
            [merchantId, sandbox]
        ); 
    },
    startPayment: function(autoStart, amount, description, email, mobile, success) {
        cordova.exec(
			success,
			null,
            'ZarinPalPlugin',
            'startPayment',
            [autoStart, amount, description, email, mobile]
        ); 
    },
    verificationPayment: function(success, error) {
        cordova.exec(
			success,
			error,
            'ZarinPalPlugin',
            'verificationPayment',
            []
        ); 
    },
    showPayment: function(success, error) {
        cordova.exec(
			success,
			error,
            'ZarinPalPlugin',
            'showPayment',
            []
        ); 
    }
};