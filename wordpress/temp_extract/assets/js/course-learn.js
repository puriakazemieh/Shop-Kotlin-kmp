/* Course player: click a lesson → play; on ended → mark complete via theme REST. */
( function () {
	'use strict';
	var cfg = window.CarmillaData || {};
	var wrap = document.getElementById( 'cl' );
	if ( ! wrap ) { return; }
	var video = document.getElementById( 'cl-video' );
	var percentEl = document.getElementById( 'cl-percent' );
	var courseId = wrap.getAttribute( 'data-id' );
	var current = null;
	var faDigits = ['۰','۱','۲','۳','۴','۵','۶','۷','۸','۹'];
	function fa( n ) { return String( n ).replace( /[0-9]/g, function ( d ) { return faDigits[ d ]; } ); }

	wrap.addEventListener( 'click', function ( e ) {
		var row = e.target.closest( '.cl-lesson' );
		if ( ! row || row.getAttribute( 'data-playable' ) !== '1' ) { return; }
		var url = row.getAttribute( 'data-url' );
		if ( ! url || ! video ) { return; }
		current = parseInt( row.getAttribute( 'data-index' ), 10 );
		video.src = url;
		video.play().catch( function () {} );
		video.scrollIntoView( { behavior: 'smooth', block: 'center' } );
	} );

	if ( video ) {
		video.addEventListener( 'ended', function () {
			if ( current === null || ! cfg.loggedIn ) { return; }
			fetch( cfg.restUrl + 'courses/' + courseId + '/lessons/' + current + '/complete', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json', 'X-WP-Nonce': cfg.nonce },
				credentials: 'same-origin'
			} ).then( function ( r ) { return r.json(); } ).then( function ( res ) {
				if ( ! res ) { return; }
				if ( percentEl && typeof res.percent !== 'undefined' ) { percentEl.textContent = fa( res.percent ); }
				var row = wrap.querySelector( '.cl-lesson[data-index="' + current + '"]' );
				if ( row ) {
					row.classList.add( 'cl-done' );
					var b = row.querySelector( '.badge' );
					if ( b ) { b.classList.remove( 'badge--new' ); b.classList.add( 'badge--stock' ); b.textContent = '✓'; }
				}
			} );
		} );
	}
} )();
