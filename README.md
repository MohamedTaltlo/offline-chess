# MT Chess 3.2.0 — Native Android
Completely native Java Android application, no WebView, HTML, JavaScript, or INTERNET permission. Local chess engine, four difficulty levels, fixed board, drag/tap moves, unlimited undo, castling, promotion, en passant, repetition, local save, native Android dialogs, four board styles. White always at bottom. 3D piece artwork: clarkerubber Staunton-Pieces, MIT license, see art/staunton-metal/NOTICE.txt. Build requires JDK17, Gradle8.9, Android SDK35 and Python CairoSVG to rasterize the bundled SVGs. Run javac -d /tmp/chess app/src/main/java/com/mt/chess/Chess.java tests/NativeChessTest.java && java -cp /tmp/chess com.mt.chess.NativeChessTest, python scripts/render_pieces.py, gradle assembleDebug. APKs are signed with the CI debug key; a previous installation signed by another key must be uninstalled before installing.

Version 3 uses original generated emerald-and-gold artwork for the launcher and ten UI icons. Bundled WebP atlases are raster source assets; scripts/render_brand.py crops them and generates Android PNG resources. Run this script alongside render_pieces.py before Gradle.

Version 3.1 uses original shaded Staunton artwork generated from the vector source in scripts/render_pieces.py. The move history is stored internally but hidden from the playing screen.

Version 3.2 uses the Trimmed 3D renders from Staunton-Pieces. Only this piece set is active. The board is natively drawn with a gold-trimmed frame, contrasting tile gradients, and outside coordinates.
