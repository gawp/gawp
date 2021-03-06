var identity_map = function () {
	emit(this.user, {refs: [this.target], count: 1});
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
	var count = this.value.count;
	var user = this._id;
	
	this.value.refs.forEach( function (ref) {
		emit(ref, {values: [{user: user, count: count}]});
	});
};

var id_reduce = function (key, values) {
	var refs = [];
	values.forEach( function (val) {
		val.values.forEach( function (v) {
			refs.push(v);
		});
	});
	return {values: refs};
}

var swap_res = db[count_res.result].mapReduce(swap_map, id_reduce);

var combine_map = function () {
    var values = this.value.values;
	values.forEach( function (val) { 
		values.forEach( function (inner) {
		
			if (val.user && inner.user && val.count && inner.count) {
				var count = val.count+inner.count;
			
				if (val.user.userId > inner.user.userId) {
					emit({user1: val.user, user2: inner.user}, {counts: [count]});
				} else if (val.user.userId < inner.user.userId) {
					emit({user1: inner.user, user2: val.user}, {counts: [count]});
				}
			}
		
		}); 
	});
}

var similarity_reduce = function (key, values) {
    var counts = [];
    var sum = 0;
    
    values.forEach( function (val) {
        val.counts.forEach( function (count) {
            sum += count;
            counts.push(count);
        });
    });
    
	var length = counts.length;
	var similarity = sum > 0 ? length / (sum - length) : 0;

	return {counts: counts, similarity: similarity};
}

var finalize = function (key, value) {
    return value.similarity;
}

var similarity_result = db[swap_res.result].mapReduce(combine_map, similarity_reduce, {finalize: finalize});

var user_map = function () {
    var user1 = this._id.user1.userId+this._id.user1.userNamespace+this._id.user1.appId;
    var user2 = this._id.user2.userId+this._id.user2.userNamespace+this._id.user2.appId;
    if (this.value > 0.0) {
        emit(user1, {neighbours: [{neighbour: this._id.user2, similarity: this.value}]});
        emit(user2, {neighbours: [{neighbour: this._id.user1, similarity: this.value}]});
    }
}

var user_reduce = function (key, values) {
    var users = [];
    values.forEach( function (val) {
        val.neighbours.forEach( function (user) {
            users.push(user);
        });
    });
    users.sort(function (a, b) { return b.similarity - a.similarity; });
    return {neighbours: users};
}

var users_res = db[similarity_result.result].mapReduce(user_map, user_reduce, {out: 'neighbours'});

db[users_res.result].find();