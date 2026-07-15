/* Support chat: load thread + send messages via theme REST. */
( function () {
	'use strict';
	var cfg = window.CarmillaData || {};
	var box = document.getElementById( 'cs' );
	if ( ! box ) { return; }
	var list = document.getElementById( 'cs-messages' );
	var form = document.getElementById( 'cs-form' );
	var input = document.getElementById( 'cs-input' );

	function bubble( m ) {
		var el = document.createElement( 'div' );
		el.className = 't-body';
		el.style.cssText = 'max-width:78%;padding:8px 12px;border-radius:var(--r-md);margin:0;' +
			( m.me
				? 'align-self:flex-start;background:var(--accent);color:var(--on-accent)'
				: 'align-self:flex-end;background:var(--surface-2);color:var(--ink)' );
		el.textContent = m.text;
		list.appendChild( el );
	}

	function scroll() { list.scrollTop = list.scrollHeight; }

	function load() {
		fetch( cfg.restUrl + 'support', { headers: { 'X-WP-Nonce': cfg.nonce }, credentials: 'same-origin' } )
			.then( function ( r ) { return r.json(); } )
			.then( function ( res ) {
				list.innerHTML = '';
				if ( res && res.messages && res.messages.length ) {
					res.messages.forEach( bubble );
				} else {
					var e = document.createElement( 'p' );
					e.className = 't-body-sm t-muted';
					e.textContent = 'گفتگو را شروع کنید؛ پشتیبانی پاسخ می‌دهد.';
					list.appendChild( e );
				}
				scroll();
			} );
	}

	form.addEventListener( 'submit', function ( e ) {
		e.preventDefault();
		var text = input.value.trim();
		if ( ! text ) { return; }
		input.value = '';
		fetch( cfg.restUrl + 'support', {
			method: 'POST',
			headers: { 'Content-Type': 'application/json', 'X-WP-Nonce': cfg.nonce },
			credentials: 'same-origin',
			body: JSON.stringify( { message: text } )
		} ).then( function ( r ) { return r.json(); } ).then( function ( m ) {
			if ( m && m.text ) {
				var empty = list.querySelector( 'p.t-muted' );
				if ( empty ) { empty.remove(); }
				bubble( m );
				scroll();
			}
		} );
	} );

	load();
} )();
