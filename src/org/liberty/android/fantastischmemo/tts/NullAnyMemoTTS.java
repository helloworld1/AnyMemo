/*
Copyright (C) 2013 Haowen Ning

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

 */
package org.liberty.android.fantastischmemo.tts;

// A stub implementation of AnyMemoTTS. All methods are stubs.
public class NullAnyMemoTTS implements AnyMemoTTS {

    public void stop() {
        // Do nothing
    }

    public void destory() {
        // Do nothing
    }

    public void sayText(final String s, final OnTextToSpeechCompletedListener onTextToSpeechCompletedListener) {
        // Do nothing, but callback completion immediately
        if (onTextToSpeechCompletedListener != null) {
            onTextToSpeechCompletedListener.onTextToSpeechCompleted(s);
        }
    }
}

