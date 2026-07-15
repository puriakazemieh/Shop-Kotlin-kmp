/* Wishlist hearts (← FavoritesScreen). Logged-in → REST (user meta);
   guests → localStorage so the heart state still persists in-browser. */
( function () {
	'use strict';
	var cfg = window.CarmillaData || {};
	var loggedIn = !! cfg.loggedIn;
	var KEY = 'cb_wishlist';

	function localIds() {
		try { return JSON.parse( localStorage.getItem( KEY ) || '[]' ); } catch ( e ) { return []; }
	}
	function saveLocal( ids ) {
		try { localStorage.setItem( KEY, JSON.stringify( ids ) ); } catch ( e ) {}
	}
	function api( path, opts ) {
		opts = opts || {};
		opts.credentials = 'same-origin';
		opts.headers = Object.assign( { 'X-WP-Nonce': cfg.nonce }, opts.headers || {} );
		return fetch( cfg.restUrl + path, opts ).then( function ( r ) { return r.json(); } );
	}

	var buttons = Array.prototype.slice.call( document.querySelectorAll( '.cb-wish-toggle' ) );
	if ( ! buttons.length ) { return; }

	function markActive( ids ) {
		var set = {};
		ids.forEach( function ( id ) { set[ id ] = true; } );
		buttons.forEach( function ( b ) {
			var on = !! set[ parseInt( b.getAttribute( 'data-id' ), 10 ) ];
			b.classList.toggle( 'is-on', on );
			b.setAttribute( 'aria-pressed', on ? 'true' : 'false' );
		} );
	}

	// Initial state.
	if ( loggedIn ) {
		api( 'wishlist' ).then( function ( r ) { markActive( ( r && r.ids ) || [] ); } );
	} else {
		markActive( localIds() );
	}

	buttons.forEach( function ( b ) {
		b.addEventListener( 'click', function ( e ) {
			e.preventDefault();
			var id = parseInt( b.getAttribute( 'data-id' ), 10 );
			if ( loggedIn ) {
				api( 'wishlist', {
					method: 'POST',
					headers: { 'Content-Type': 'application/json' },
					body: JSON.stringify( { productId: id } )
				} ).then( function ( r ) { markActive( ( r && r.ids ) || [] ); } );
			} else {
				var ids = localIds();
				var pos = ids.indexOf( id );
				if ( pos === -1 ) { ids.push( id ); } else { ids.splice( pos, 1 ); }
				saveLocal( ids );
				markActive( ids );
			}
		} );
	} );
} )();
