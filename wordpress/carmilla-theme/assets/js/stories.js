/* Fullscreen story viewer: tap/keys to advance, auto-progress, CTA link. */
( function () {
	'use strict';
	var openers = Array.prototype.slice.call( document.querySelectorAll( '.cb-story-open' ) );
	var viewer = document.getElementById( 'cb-story-viewer' );
	if ( ! openers.length || ! viewer ) { return; }

	var img = document.getElementById( 'cb-sv-img' );
	var title = document.getElementById( 'cb-sv-title' );
	var content = document.getElementById( 'cb-sv-content' );
	var cta = document.getElementById( 'cb-sv-cta' );
	var barsHost = document.getElementById( 'cb-sv-bars' );
	var DURATION = 5000;
	var idx = 0, timer = null, start = 0;

	var stories = openers.map( function ( b ) {
		return {
			image: b.getAttribute( 'data-image' ),
			title: b.getAttribute( 'data-title' ),
			content: b.getAttribute( 'data-content' ),
			link: b.getAttribute( 'data-link' )
		};
	} );

	function buildBars() {
		barsHost.innerHTML = '';
		stories.forEach( function () {
			var t = document.createElement( 'span' );
			t.className = 'cb-sv__bar';
			t.innerHTML = '<span class="cb-sv__bar-fill"></span>';
			barsHost.appendChild( t );
		} );
	}

	function paintBars( active, ratio ) {
		var bars = barsHost.children;
		for ( var i = 0; i < bars.length; i++ ) {
			var fill = bars[ i ].firstChild;
			fill.style.width = i < active ? '100%' : ( i === active ? ( ratio * 100 ) + '%' : '0%' );
		}
	}

	function tick() {
		var elapsed = Date.now() - start;
		var ratio = Math.min( elapsed / DURATION, 1 );
		paintBars( idx, ratio );
		if ( ratio >= 1 ) { next(); return; }
		timer = requestAnimationFrame( tick );
	}

	function show( i ) {
		if ( i < 0 ) { i = 0; }
		if ( i >= stories.length ) { close(); return; }
		idx = i;
		var s = stories[ i ];
		img.src = s.image || '';
		img.alt = s.title || '';
		title.textContent = s.title || '';
		content.textContent = s.content || '';
		if ( s.link ) { cta.href = s.link; cta.hidden = false; } else { cta.hidden = true; }
		start = Date.now();
		cancelAnimationFrame( timer );
		timer = requestAnimationFrame( tick );
	}

	function next() { show( idx + 1 ); }
	function prev() { show( idx - 1 ); }

	function open( i ) {
		buildBars();
		viewer.hidden = false;
		viewer.setAttribute( 'aria-hidden', 'false' );
		document.documentElement.style.overflow = 'hidden';
		show( i );
	}

	function close() {
		cancelAnimationFrame( timer );
		viewer.hidden = true;
		viewer.setAttribute( 'aria-hidden', 'true' );
		document.documentElement.style.overflow = '';
	}

	openers.forEach( function ( b, i ) {
		b.addEventListener( 'click', function () { open( i ); } );
	} );
	document.getElementById( 'cb-sv-close' ).addEventListener( 'click', close );
	document.getElementById( 'cb-sv-next' ).addEventListener( 'click', next );
	document.getElementById( 'cb-sv-prev' ).addEventListener( 'click', prev );
	document.addEventListener( 'keydown', function ( e ) {
		if ( viewer.hidden ) { return; }
		if ( e.key === 'Escape' ) { close(); }
		else if ( e.key === 'ArrowLeft' ) { next(); }   // RTL: left = forward
		else if ( e.key === 'ArrowRight' ) { prev(); }
	} );
} )();
