var identity_map = function () {
	emit(this.user.userId, {refs: [this.target.ref], count: 1});
};

var count_reduce = function (key, values) {
	var refs = [];
	values.forEach( function (val) {
		val.refs.forEach( function (ref) {
			refs.push(ref);
		});
	});
	
	return {refs: refs, count: refs.length};
};

var count_res = db.consumption.mapReduce(identity_map, count_reduce);

var swap_map = function () {
	var count = this.value.refs.count;
	var user = this._id;
	
	this.value.refs.forEach( function (ref) {
			emit(ref, {values: [{user: user, count: count}]});
	});
};

var id_reduce = function (key, values) {
	var refs = [];
	values.forEach( function (val) {
		val.values.forEach( function (v) {
			print (v);
			refs.push(v);
		});
	});
	print("refs: "+refs);
	return {values: refs};
}

var swap_res = db[count_res.result].mapReduce(swap_map, id_reduce);

var combine_map = function () {
	var values = this.value.values[0].values;
	
	if (typeof values == "object" && values.length && values.length > 1) {
		values.forEach( function (val) { 
			values.forEach( function (inner) {
				
				if (val.user && inner.user && val.count && inner.count) {
					var count = val.count+inner.count;
					
					if (val.user > inner.user) {
						emit({user1: val.user, user2: inner.user}, count);
					} else if (val.user < inner.user) {
						emit({user1: inner.user, user2: val.user}, count);
					}
				}
				
			}); 
		});
	}
}

var similarity_reduce = function (key, values) {
	if (typeof values == "object" && values.length) {
		var sum = 0;
		for (var i = 0; i < values.length; i++) {
		  sum += (typeof values[i] == 'number') ? values[i] : 0;
		}
		var length = values.length;
		var similarity = sum > 0 ? length / (sum - length) : 0;
	
		return {values: values, similarity: similarity};
	}
}

var similarity_result = db[swap_res.result].mapReduce(combine_map, similarity_reduce);

db[similarity_result.result].find();