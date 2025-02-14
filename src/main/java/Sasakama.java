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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.icn.sasakama.Engine;
import org.icn.sasakama.Misc;


public class Sasakama {
    final static String progname = "Sasakama";
    final static String version = "version: 20151228";

    public static void usage() {
        System.err.printf("%s - The HMM-based speech synthesis engine %s \n", progname, version);
        System.err.print("\n");
        System.err.print("  usage:\n");
        System.err.printf("    %s [ options ] [ infile ]\n", progname);
        System.err.print("  options:                                                                   [  def][ min-- max]\n");
        System.err.print("    -m  htsvoice   : HTS voice files                                         [  N/A]\n");
        System.err.print("    -od s          : filename of output label with duration                  [  N/A]\n");
        System.err.print("    -om s          : filename of output spectrum                             [  N/A]\n");
        System.err.print("    -of s          : filename of output log F0                               [  N/A]\n");
        System.err.print("    -ol s          : filename of output low-pass filter                      [  N/A]\n");
        System.err.print("    -or s          : filename of output raw audio (generated speech)         [  N/A]\n");
        System.err.print("    -ow s          : filename of output wav audio (generated speech)         [  N/A]\n");
        System.err.print("    -ot s          : filename of output trace information                    [  N/A]\n");
        System.err.print("    -vp            : use phoneme alignment for duration                      [  N/A]\n");
        System.err.print("    -i  i f1 .. fi : enable interpolation & specify number(i),coefficient(f) [  N/A]\n");
        System.err.print("    -s  i          : sampling frequency                                      [ auto][   1--    ]\n");
        System.err.print("    -p  i          : frame period (point)                                    [ auto][   1--    ]\n");
        System.err.print("    -a  f          : all-pass constant                                       [ auto][ 0.0-- 1.0]\n");
        System.err.print("    -b  f          : postfiltering coefficient                               [  0.0][ 0.0-- 1.0]\n");
        System.err.print("    -r  f          : speech speed rate                                       [  1.0][ 0.0--    ]\n");
        System.err.print("    -fm f          : additional half-tone                                    [  0.0][    --    ]\n");
        System.err.print("    -u  f          : voiced/unvoiced threshold                               [  0.5][ 0.0-- 1.0]\n");
        System.err.print("    -jm f          : weight of GV for spectrum                               [  1.0][ 0.0--    ]\n");
        System.err.print("    -jf f          : weight of GV for log F0                                 [  1.0][ 0.0--    ]\n");
        System.err.print("    -g  f          : volume (dB)                                             [  0.0][    --    ]\n");
        System.err.print("    -z  i          : audio buffer size (if i==0, turn off)                   [    0][   0--    ]\n");
        System.err.print("  infile:\n");
        System.err.print("    label file\n");
        System.err.print("  note:\n");
        System.err.print("    generated spectrum, log F0, and low-pass filter coefficient\n");
        System.err.print("    sequences are saved in natural endian, binary (float) format.\n");
        System.err.print("\n");

        System.exit(0);
    }

    public static void main(String[] args) {
        int num_voices;
        List<String> arrayStr = new ArrayList<>();

        if (args.length == 0) {
            usage();
        }

        for (int i = 0; i < args.length; i++)
            if (args[i].equals("-m"))
                arrayStr.add(args[++i]);

        num_voices = arrayStr.size();
        if (num_voices == 0) {
            Misc.error("Error: HTS voices cannot be loaded.\n");
            System.exit(1);
        }

        String[] voices = new String[num_voices];
        for (int i = 0; i < num_voices; i++)
            voices[i] = arrayStr.get(i);

        Engine engine = new Engine();
        if (!engine.load(voices)) {
            Misc.error("Error: HTS voices cannot be loaded.");
        }

        String labfn = null;
        int num_interpolation_weights;
        FileOutputStream wavfp = null;
        FileOutputStream rawfp = null;
        FileOutputStream durfp = null;
        FileOutputStream mgcfp = null;
        FileOutputStream lf0fp = null;
        FileOutputStream lpffp = null;
        FileOutputStream tracefp = null;
        boolean use_audio = false;

        int cnt = 0;
        try {
            while (cnt < args.length) {
                if (args[cnt].equals("-vp")) {
                    engine.set_phoneme_alignment_flag(true);
                } else if (args[cnt].equals("-ow")) {
                    wavfp = new FileOutputStream(args[++cnt]);
                } else if (args[cnt].equals("-od")) {
                    durfp = new FileOutputStream(args[++cnt]);
                } else if (args[cnt].equals("-om")) {
                    mgcfp = new FileOutputStream(args[++cnt]);
                } else if (args[cnt].equals("-of") || args[cnt].equals("-op")) {
                    lf0fp = new FileOutputStream(args[++cnt]);
                } else if (args[cnt].equals("-ol")) {
                    lpffp = new FileOutputStream(args[++cnt]);
                } else if (args[cnt].equals("-ot")) {
                    tracefp = new FileOutputStream(args[++cnt]);
                } else if (args[cnt].equals("-or")) {
                    rawfp = new FileOutputStream(args[++cnt]);
                } else if (args[cnt].equals("-h")) {
                    usage();
                } else if (args[cnt].equals("-m")) {
                    cnt++;
                } else if (args[cnt].equals("-s")) {
                    engine.set_sampling_frequency(Integer.parseInt(args[++cnt]));
                } else if (args[cnt].equals("-p")) {
                    engine.set_fperiod(Integer.parseInt(args[++cnt]));
                } else if (args[cnt].equals("-a")) {
                    engine.set_alpha(Double.parseDouble(args[++cnt]));
                } else if (args[cnt].equals("-b")) {
                    engine.set_beta(Double.parseDouble(args[++cnt]));
                } else if (args[cnt].equals("-r")) {
                    engine.set_speed(Double.parseDouble(args[++cnt]));
                } else if (args[cnt].equals("-fm")) {
                    engine.add_half_tone(Double.parseDouble(args[++cnt]));
                } else if (args[cnt].equals("-u")) {
                    engine.set_msd_threshold(1, Double.parseDouble(args[++cnt]));
                } else if (args[cnt].equals("-i")) {
                    num_interpolation_weights = Integer.parseInt(args[++cnt]);
                    if (num_interpolation_weights != num_voices) {
                        Misc.error("num_interpolation_weights != num_voices");
                        System.exit(1);
                    }
                    for (int j = 0; j < num_interpolation_weights; j++) {
                        double f = Double.parseDouble(args[++cnt]);
                        engine.set_duration_interpolation_weight(j, f);
                        engine.set_parameter_interpolation_weight(j, 0, f);
                        engine.set_parameter_interpolation_weight(j, 1, f);
                        engine.set_gv_interpolation_weight(j, 0, f);
                        engine.set_gv_interpolation_weight(j, 1, f);
                    }
                } else if (args[cnt].equals("-jm")) {
                    engine.set_gv_weight(0, Double.parseDouble(args[++cnt]));
                } else if (args[cnt].equals("-jf") || args[cnt].equals("-jp")) {
                    engine.set_gv_weight(1, Double.parseDouble(args[++cnt]));
                } else if (args[cnt].equals("-g")) {
                    engine.set_volume(Double.parseDouble(args[++cnt]));
                } else if (args[cnt].equals("-z")) {
                    int bufsize = Integer.parseInt(args[++cnt]);
                    engine.set_audio_buff_size(bufsize);
                    if (bufsize > 0)
                        use_audio = true;
                } else if (args[cnt].startsWith("-")) {
                    Misc.error("Error: Invalid option: " + args[cnt]);
                    System.exit(1);
                } else {
                    labfn = args[cnt];
                }
                cnt++;
            }
        } catch (NumberFormatException | FileNotFoundException e1) {
            e1.printStackTrace();
        }

        // synthesize;
        try {
            if (!engine.synthesize_from_fn(labfn)) {
                Misc.error(" waveform cannot be synthesized.");
                System.exit(1);
            }
            if (tracefp != null) {
                engine.save_information(tracefp);
                tracefp.close();
            }

            if (durfp != null) {
                engine.save_label(durfp);
                durfp.close();
            }

            if (rawfp != null) {
                engine.save_generated_speech(rawfp);
                rawfp.close();
            }

            if (wavfp != null) {
                engine.save_riff(wavfp);
                wavfp.close();
            }

            if (mgcfp != null) {
                engine.save_generated_parameter(0, mgcfp);
                mgcfp.close();
            }

            if (lf0fp != null) {
                engine.save_generated_parameter(1, lf0fp);
                lf0fp.close();
            }

            if (lpffp != null) {
                engine.save_generated_parameter(2, lpffp);
                lpffp.close();
            }
            if (use_audio) {
                engine.close_audio();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
