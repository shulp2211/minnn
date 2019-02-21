.. include:: reference_descriptions/consensus-header.rst

Consensus action aligns multiple sequences by searching the most frequent K-mer in all of these sequences and then
placing all of these sequences to the same coordinate system by offset of the most frequent K-mer in each of
the sequences. Sequences where the most frequent K-mer (with allowed number of errors specified by
:code:`--kmer-max-errors` argument) is not found are marked as remaining sequences and not included in current
consensus calculation. Then consensus action calculates consensus from the current cluster of sequences that are
already positioned in the same coordinate system. After this, quality trimming is performed on both sides of the
consensus, and if the resulting sequence is too short, this consensus is discarded. But if there are many remaining
sequences (both in case that consensus was calculated and in case it was discarded), next consensus calculation will
start with the cluster of remaining sequences.

:code:`--groups` argument with space separated list of groups must be specified in consensus action to specify which
groups will be used for consensus calculation. This argument is mandatory. :code:`--input` and :code:`--output`
arguments are optional, and if they are missing, stdin and stdout will be used instead of input and output files.

Examples for consensus action:

.. code-block:: text

   minnn consensus --groups SB1 SB2 --input sorted.mif --output consensus.mif --score-threshold -750
   xzcat sorted.mif.xz | minnn consensus --output consensus.mif
   minnn consensus --max-consensuses-per-cluster 1 --consensuses-to-separate-groups --input data.mif --output result.mif

:code:`--score-threshold` is very important option: it is alignment score threshold by which it will be determined
to include sequence to the consensus or don't include. Too low threshold score can result in including garbage data
to consensus or melding multiple molecules to one consensus. Too high threshold score can result in splitting one
molecule to multiple consensuses, and in leaving out sequences with valuable data. Score values for single matches,
mismatches and indels in alignment can be set with :code:`--aligner-match-score`, :code:`--aligner-mismatch-score` and
:code:`--aligner-gap-score` arguments.

Consensus action uses banded aligner, it reduces size of the matrix to increase speed and reduce memory usage.
:code:`--width` option allows to specify width of the banded matrix. Lower values mean faster consensus calculation
and lower memory usage, but less accuracy. It's recommended to set :code:`--width` to maximum allowed length of
single insertion or deletion, multiplied by 1.5.

:code:`--max-consensuses-per-cluster` is important option: it allows to set maximum number of consensuses for
one combination of barcode values. If each molecule is marked by unique set of barcodes, you can set
:code:`--max-consensuses-per-cluster` to :code:`1`.

If enough sequences from cluster were not used in consensus calculations, and :code:`--max-consensuses-per-cluster` is
not exceeded, then new consensus calculation will start from remaining sequences. You can set the threshold with
:code:`--skipped-fraction-to-repeat` option. If number of remaining sequences divided to cluster size is not below
this threshold, new consensus calculation will start.

Calculated consensuses are processed with quality trimmer to remove low quality tails on the left and right sides.
You can set trim window size (length of subsequence for which average quality is calculated) with
:code:`--trim-window-size` option. Quality threshold is set by :code:`--avg-quality-threshold` option, and minimal
allowed consensus length after trimming - by :code:`--min-good-sequence-length` option. If resulting consensus will be
shorter, it will be discarded.

Input reads are prepared with the same quality trimmer before calculating consensus, but trimming parameters are
configured separately. Use :code:`--reads-trim-window-size` to set trim window size for input reads,
:code:`--reads-avg-quality-threshold` for quality threshold and :code:`--reads-min-good-sequence-length` for minimum
input read length after trimming. Too short reads will not be included in consensus calculation.

:code:`--consensuses-to-separate-groups` parameter changes consensus action behavior significantly. If this parameter
is not specified, output file will contain calculated consensuses. If it is specified, original sequences will be
written to the output files, consensuses will be written as capture groups :code:`CR1`, :code:`CR2` etc, so it will be
possible to cluster original reads by consensuses using :ref:`filter` / :ref:`demultiplex` actions, or export original
reads and corresponding consensuses into separate reads using :ref:`mif2fastq` action. Note that input file must
not contain any groups named :code:`CR1`, :code:`CR2` etc if you use :code:`--consensuses-to-separate-groups`
parameter.

Command line arguments reference for consensus action:
