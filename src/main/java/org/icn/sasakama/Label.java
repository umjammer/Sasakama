// This software is a language translation version of "hts_engine API" developed by HTS Working Group.
// 
// Copyright (c) 2015 Intelligent Communication Network (Ito-Nose) Laboratory
// Tohoku University
// Copyright (c) 2001-2015 Nagoya Institute of Technology
// Department of Computer Science
// 2001-2008 Tokyo Institute of Technology
// Interdisciplinary Graduate School of
// Science and Engineering
// 
// All rights reserved.
// 
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
// * Redistributions of source code must retain the above copyright notice, 
// this list of conditions and the following disclaimer.
// * Redistributions in binary form must reproduce the above copyright notice, 
// this list of conditions and the following disclaimer in the documentation 
// and/or other materials provided with the distribution.
// * Neither the name of the "Intelligent Communication Network Laboratory, Tohoku University" nor the names of its contributors 
// may be used to endorse or promote products derived from this software 
// without specific prior written permission.
// 
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
// DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package org.icn.sasakama;

import java.util.logging.Level;

import vavi.util.Debug;


public class Label {
    LabelString head;
    int size;

    public Label() {
    }

    public void initialize() {
        head = null;
        size = 0;
    }

    public void check_time() {
        LabelString lstring = head;

        if (lstring != null)
            lstring.start = 0.0;
        while (lstring != null) {
            LabelString next = lstring.next;
            if (next == null)
                break;
            if (lstring.end < 0.0 && next.start >= 0.0)
                lstring.end = next.start;
            else if (lstring.end >= 0.0 && next.start < 0.0)
                next.start = lstring.end;
            if (lstring.start < 0.0)
                lstring.start = -1.0;
            if (lstring.end < 0.0)
                lstring.end = -1.0;
            lstring = next;
        }
    }

    public void load(int sampling_rate, int fperiod, File hf) {
        LabelString lstring = null;

        double rate = (double) sampling_rate / ((double) fperiod * Constant.TIME_CONSTANT);

        if (head != null || size != 0) {
            Misc.error("Label.load: label is uninitialized.");
            return;
        }

        // parse label file
        while (true) {
            StringBuffer buff = new StringBuffer();
            hf.get_token(buff);
            if (!Misc.is_graph(buff.toString()))
                break;
            Debug.printf(Level.FINER, "str:[%s]", buff);
            size++;

            if (lstring != null) {
                lstring.next = new LabelString();
                lstring = lstring.next;
            } else {
                lstring = new LabelString();
                head = lstring;
            }
            if (Misc.is_num(buff.toString())) {
                double st = Double.parseDouble(buff.toString());
                hf.get_token(buff);
                Debug.printf(Level.FINER, "tok1:%s", buff);
                double ed = Double.parseDouble(buff.toString());
                hf.get_token(buff);
                Debug.printf(Level.FINER, "tok2:%s", buff);

                lstring.start = rate * st;
                lstring.end = rate * ed;
            } else {
                lstring.start = -1.0;
                lstring.end = -1.0;
            }
            lstring.next = null;
            lstring.name = buff.toString();
        }
        Debug.printf(Level.FINER, "label load break while");
        check_time();
    }

    public void load_from_fn(int sampling_rate, int fperiod, String fn) {
        File hf = new File();
        hf.open(fn, "r");
        load(sampling_rate, fperiod, hf);
        hf.close();
    }

    public void load_from_strings(int sampling_rate, int fperiod, String[] lines) {
        LabelString lstring = null;
        double rate = (double) sampling_rate / ((double) fperiod * Constant.TIME_CONSTANT);
        double st, ed;

        if (head != null || size != 0) {
            Misc.error("load_from_strings: label list is not initialized.");
            return;
        }

        // copy label
        for (String line : lines) {
            if (!Misc.is_graph(line.substring(0, 1)))
                break;
            size++;

            if (lstring != null) {
                lstring.next = new LabelString();
                lstring = lstring.next;
            } else {
                lstring = new LabelString();
                head = lstring;
            }
            int[] data_index = new int[] {0};
            StringBuffer sb = new StringBuffer();
            if (Misc.is_num(line)) {
                Misc.get_token_from_string(line, data_index, sb);
                st = Double.parseDouble(sb.toString());
                Misc.get_token_from_string(line, data_index, sb);
                ed = Double.parseDouble(sb.toString());
                Misc.get_token_from_string(line, data_index, sb);
                lstring.name = sb.toString();
                lstring.start = rate * st;
                lstring.end = rate * ed;
            } else {
                lstring.start = -1.0;
                lstring.end = -1.0;
                lstring.name = line;
            }
            lstring.next = null;
        }
        check_time();
    }

    public int get_size() {
        return size;
    }

    public final String get_string(int index) {
        LabelString lstring = head;

        for (int i = 0; i < index && lstring != null; i++)
            lstring = lstring.next;
        if (lstring == null)
            return null;

        return lstring.name;
    }

    public double get_start_frame(int index) {
        LabelString lstring = head;

        for (int i = 0; i < index && lstring != null; i++)
            lstring = lstring.next;
        if (lstring == null)
            return -1.0;
        return lstring.start;
    }

    public double get_end_frame(int index) {
        LabelString lstring = head;

        for (int i = 0; i < index && lstring != null; i++)
            lstring = lstring.next;
        if (lstring == null)
            return -1.0;
        return lstring.end;
    }

    public void clear() {
        head = null;
        initialize();
    }
}
