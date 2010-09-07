var identity_map = function () {
	emit(this.user.userId, this.target.ref);
};

var count_reduce = function (key, values) {
	return {refs: values, count: values.length};
};

var count_res = db.consumption.mapReduce(identity_map, count_reduce);

var swap_map = function () {
	var count = this.value.refs[0].count;
	var user = this._id;
	
	this.value.refs[0].refs.forEach(function (ref) {emit(ref, {user: user, count: count});});
};

var id_reduce = function (key, values) {
	return {values: values};
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