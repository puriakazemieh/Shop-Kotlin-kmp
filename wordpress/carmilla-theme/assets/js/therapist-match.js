/* Therapist match: pick a concern chip → filtered therapist suggestions. */
( function () {
	'use strict';
	var cfg = window.CarmillaData || {};
	var concerns = ( window.CarmillaMatch && window.CarmillaMatch.concerns ) || {};
	var chips = document.getElementById( 'cb-match-chips' );
	var results = document.getElementById( 'cb-match-results' );
	if ( ! chips || ! results ) { return; }

	function esc( s ) { var d = document.createElement( 'div' ); d.textContent = s == null ? '' : s; return d.innerHTML; }

	Object.keys( concerns ).forEach( function ( key ) {
		var b = document.createElement( 'button' );
		b.type = 'button';
		b.className = 'chip';
		b.textContent = concerns[ key ];
		b.addEventListener( 'click', function () {
			Array.prototype.forEach.call( chips.children, function ( c ) { c.classList.remove( 'is-on' ); } );
			b.classList.add( 'is-on' );
			load( key );
		} );
		chips.appendChild( b );
	} );

	function load( concern ) {
		results.innerHTML = '<p class="t-body-sm t-muted">در حال جست‌وجو…</p>';
		fetch( cfg.restUrl + 'therapist-match?concern=' + encodeURIComponent( concern ), { credentials: 'same-origin' } )
			.then( function ( r ) { return r.json(); } )
			.then( function ( items ) {
				if ( ! items || ! items.length ) {
					results.innerHTML = '<p class="t-body-sm t-muted">مشاوری یافت نشد.</p>';
					return;
				}
				results.innerHTML = items.map( function ( it ) {
					var img = it.image ? '<a href="' + esc( it.permalink ) + '" class="thumb"><img src="' + esc( it.image ) + '" alt=""></a>' : '';
					return '<article class="card">' + img + '<div class="card--pad"><h3 class="t-title-sm"><a href="' +
						esc( it.permalink ) + '">' + esc( it.name ) + '</a></h3>' +
						( it.specialty ? '<div class="meta-row"><span class="badge badge--new">' + esc( it.specialty ) + '</span></div>' : '' ) +
						'</div></article>';
				} ).join( '' );
			} );
	}
} )();
