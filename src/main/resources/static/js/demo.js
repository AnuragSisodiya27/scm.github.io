console.log("working...");

const toggleSidebar =()=> {

	if ($(".sidebar").is(":visible")) {
		
		$(".sidebar").css("display", "none");
		$(".content").css("margin-left", "1%");
		
	} else {
		
		$(".sidebar").css("display", "block");
		$(".content").css("margin-left", "20%");
		
		//$(".menuBtn").css("margin-left", "273px");
		$(".menuBtn").css("display", "block");
		
	}

};

const search = () => {
	console.log("Searching...");
	let query = $("#search-input").val();
	console.log(query);

	if (query == "") {
		$(".search-result").hide();
	} else {
		console.log(query);

		//sending request to server
		let url = `http://localhost:8080/search/${query}`;

		fetch(url).then((response) => {
			return response.json();
		})
			.then((data) => {
				//accessing data....
				//console.log(data);

				let text = `<div class='list-group'>`;

				data.forEach((contact) => {
					text += `<a href='/user/contact/${contact.cid}/' class='list-group-item list-group-item-action'>${contact.name}</a>`
				});

				text += `</div>`;

				$(".search-result").html(text);
				$(".search-result").show();

			});

		$(".search-result").show();
	}

};


//first request - to create order for payment

const paymentStart = () => {

	console.log("Payment Started");

	let amount = $("#payment_field").val();

	console.log(amount);

	if (amount == '' || amount == null) {
		alert("Amount is Required !!");
		return;
	}

	//code..
	//we will use ajax to send request to server to create order -- using jquery

	$.ajax({
		url: '/users/create_order',
		data: JSON.stringify({ amount: amount, info: 'order_request' }),
		contentType: 'application/json',
		type: 'POST',
		dataType: 'json',
		success: function (response) {
			//invoked when success
			console.log(response);
			if (response.status == 'created') {
				//open payment form

				let options = {
					key: 'rzp_test_X52fQcBrNkt0Vc',
					amount: response.amount,
					currency: 'INR',
					name: 'ContactManager',
					description: 'Donation',
					image: 'D:/Swarjali/batchparty/IMG20220530173916.jpg',
					order_id: response.id,
					handler: function (response) {
						console.log(response.razorpay_payment_id);
						console.log(response.razorpay_order_id);
						console.log(response.razorpay_signature);
						console.log("payment successful");

						updatePaymentOnServer(response.razorpay_payment_id,response.razorpay_order_id,'paid');

					},
					prefill: {
						name: "",
						email: "",
						contact: ""
					},
					notes: {
						address: "Sisodiya House",
					},
					theme: {
						color: "#3399cc"
					}
				};

				let rzp = new Razorpay(options);
				rzp.on('payment.failed', function (response) {
					console.log(response.error.code);
					console.log(response.error.description);
					console.log(response.error.source);
					console.log(response.error.step);
					console.log(response.error.reason);
					console.log(response.error.metadata.order_id);
					console.log(response.error.metadata.payment_id);
				
				});

				rzp.open();
			}
		},
		error: function (error) {
			//invoked when error
			console.log(error);
			alert("Something Went Wrong !!")
		}

	})


};


//
function updatePaymentOnServer(payment_id,order_id,status){
	$.ajax({
		url: '/users/update_order',
		data: JSON.stringify({ payment_id: payment_id, order_id: order_id, status:status}),
		contentType: 'application/json',
		type: 'POST',
		dataType: 'json',
		success:function(response){
			alert('Congrats !! Payment Done Successfully!!')
		},
		error:function(error){
			alert("Oops Payment failed!!");
		},
	});
}