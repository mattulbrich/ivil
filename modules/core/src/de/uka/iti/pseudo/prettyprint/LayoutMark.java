package de.uka.iti.pseudo.prettyprint;

import de.uka.iti.pseudo.prettyprint.AnnotatedString.Style;

interface LayoutMark {
    public void handle(AnnotatedString as);

    class BeginTerm implements LayoutMark {
        private final int subtermno;

        public BeginTerm(int subtermno) {
            this.subtermno = subtermno;
        }

        @Override
        public void handle(AnnotatedString as) {
            as.handleBeginTerm(subtermno);
        }
    }

    class EndTerm implements LayoutMark {
        @Override
        public void handle(AnnotatedString as) {
            as.handleEndTerm();
        }
    }

    class PushStyle implements LayoutMark {
        private final Style style;

        public PushStyle(Style style) {
            this.style = style;
        }

        @Override
        public void handle(AnnotatedString as) {
            as.handlePushStyle(style);
        }
    }

    class PopStyle implements LayoutMark {
        @Override
        public void handle(AnnotatedString as) {
            as.handlePopStyle();
        }
    }
}