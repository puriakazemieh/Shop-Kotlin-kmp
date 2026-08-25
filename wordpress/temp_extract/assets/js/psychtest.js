/* Take-test flow: collect answers, submit to theme REST, render interpretation. */
( function () {
	'use strict';
	var cfg = window.CarmillaData || {};
	var faDigits = ['۰','۱','۲','۳','۴','۵','۶','۷','۸','۹'];
	function fa( n ) { return String( n ).replace( /[0-9]/g, function ( d ) { return faDigits[ d ]; } ); }

	var form = document.getElementById( 'pt-form' );
	if ( ! form ) { return; }
	var result = document.getElementById( 'pt-result' );

	form.addEventListener( 'submit', function ( e ) {
		e.preventDefault();
		var groups = {};
		form.querySelectorAll( 'input[type=radio]' ).forEach( function ( r ) {
			var m = r.name.match( /^q(\d+)$/ );
			if ( m ) { groups[ m[1] ] = groups[ m[1] ] || null; if ( r.checked ) { groups[ m[1] ] = parseInt( r.value, 10 ); } }
		} );
		var keys = Object.keys( groups );
		var answered = keys.filter( function ( k ) { return groups[ k ] !== null; } );
		if ( answered.length < keys.length ) {
			result.style.display = 'block';
			result.innerHTML = '<p class="t-body" style="color:var(--sale);margin:0">لطفاً به همه‌ی سؤال‌ها پاسخ دهید.</p>';
			result.scrollIntoView( { behavior: 'smooth' } );
			return;
		}
		var answers = keys.sort( function ( a, b ) { return a - b; } ).map( function ( k ) { return groups[ k ]; } );

		fetch( cfg.restUrl + 'psych-tests/' + form.getAttribute( 'data-id' ) + '/submit', {
			method: 'POST',
			headers: { 'Content-Type': 'application/json', 'X-WP-Nonce': cfg.nonce },
			credentials: 'same-origin',
			body: JSON.stringify( { answers: answers } )
		} ).then( function ( r ) { return r.json(); } ).then( function ( res ) {
			result.style.display = 'block';
			if ( res && typeof res.score !== 'undefined' ) {
				result.innerHTML =
					'<p class="t-caption">نتیجه‌ی شما</p>' +
					'<h2 class="t-title-lg" style="margin:4px 0">امتیاز: ' + fa( res.score ) + ' از ' + fa( res.maxScore ) + '</h2>' +
					( res.interpretation ? '<p class="t-body" style="margin:0">' + res.interpretation + '</p>' : '' );
			} else {
				result.innerHTML = '<p class="t-body" style="color:var(--sale);margin:0">' + ( ( res && res.message ) || 'خطا در ثبت پاسخ.' ) + '</p>';
			}
			result.scrollIntoView( { behavior: 'smooth' } );
		} );
	} );
} )();
