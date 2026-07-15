/* Course requests: submit + like via the theme REST routes (carmilla/v1). */
( function () {
	'use strict';
	var cfg = window.CarmillaData || {};
	var faDigits = ['۰','۱','۲','۳','۴','۵','۶','۷','۸','۹'];
	function fa( n ) { return String( n ).replace( /[0-9]/g, function ( d ) { return faDigits[ d ]; } ); }

	function api( path, method ) {
		return fetch( cfg.restUrl + path, {
			method: method || 'GET',
			headers: { 'Content-Type': 'application/json', 'X-WP-Nonce': cfg.nonce },
			credentials: 'same-origin',
			body: arguments[ 2 ] ? JSON.stringify( arguments[ 2 ] ) : undefined
		} ).then( function ( r ) { return r.json(); } );
	}

	// Like toggling (event delegation).
	var list = document.getElementById( 'cr-list' );
	if ( list ) {
		list.addEventListener( 'click', function ( e ) {
			var btn = e.target.closest( '.cr-like' );
			if ( ! btn ) { return; }
			if ( ! cfg.loggedIn ) { window.location = cfg.loginUrl; return; }
			var id = btn.getAttribute( 'data-id' );
			btn.disabled = true;
			api( 'course-requests/' + id + '/like', 'POST' ).then( function ( res ) {
				if ( res && typeof res.likeCount !== 'undefined' ) {
					btn.setAttribute( 'aria-pressed', res.liked ? 'true' : 'false' );
					btn.querySelector( '.cnt' ).textContent = fa( res.likeCount );
				}
			} ).finally( function () { btn.disabled = false; } );
		} );
	}

	// Submit new request.
	var form = document.getElementById( 'cr-form' );
	if ( form ) {
		form.addEventListener( 'submit', function ( e ) {
			e.preventDefault();
			var title = document.getElementById( 'cr-title' ).value.trim();
			var desc = document.getElementById( 'cr-desc' ).value.trim();
			if ( ! title ) { return; }
			api( 'course-requests', 'POST', { title: title, description: desc } ).then( function ( dto ) {
				if ( ! dto || ! dto.id ) { return; }
				var empty = list.querySelector( '.empty-state' );
				if ( empty ) { empty.remove(); }
				var el = document.createElement( 'article' );
				el.className = 'card card--pad';
				el.setAttribute( 'data-id', dto.id );
				el.style.cssText = 'display:flex;align-items:center;justify-content:space-between;gap:var(--sp-md)';
				el.innerHTML = '<div><h3 class="t-title" style="margin:0"></h3><p class="t-body-sm t-muted" style="margin:4px 0 0"></p></div>' +
					'<button class="chip cr-like" aria-pressed="false" data-id="' + dto.id + '">♥ <span class="cnt">۰</span></button>';
				el.querySelector( 'h3' ).textContent = dto.title;
				el.querySelector( 'p' ).textContent = dto.description || '';
				list.prepend( el );
				form.reset();
			} );
		} );
	}
} )();
