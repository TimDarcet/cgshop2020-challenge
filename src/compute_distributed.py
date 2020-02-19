#!/usr/bin/env python3

import click
from pathlib import Path
import subprocess
import datetime
from time import sleep
import sys


@click.command()
@click.option(
    '-f',
    "--folder-to-compute",
    type=click.Path(
        exists=True,
        file_okay=False,
        dir_okay=True
    ),
    required=True,
    help="Path to folder to be computed using code"
)
@click.option(
    '-c',
    "--computers-file",
    type=click.File(),
    default="~/computers_name",
    help="A file containing the ssh connection addresses of the other computers to distribute on"
)
def compute(folder_to_compute, computers_file):
    """
    Distributes the computation
    """
    ##### Encoding number 1 #####
    folder_to_compute = Path(folder_to_compute).resolve()
    # Read computer list
    computers = list(map(str.strip, computers_file.readlines()))
    # Make folders
    cmpidx = 0
    output_folder = folder_to_compute.parent / 'outputs'
    output_folder.mkdir(exist_ok=True)
    locks_folder = Path.cwd() / 'locks'
    locks_folder.mkdir(exist_ok=True)
    for cloud in folder_to_compute.rglob('*.json'):
        # Get out file
        out_file = output_folder / cloud.relative_to(folder_to_compute)
        # Create parent folders
        out_file.parent.mkdir(exist_ok=True, parents=True)
        # Check it has not already been computed
        if not out_file.is_file():
            lockname = str(cloud.name) + '@' + cmp.split('@')[-1]



###################################################
########## The rest of this file is TODO ##########
###################################################




            # Get a computer
            cmp = computers[cmpidx % len(computers)]
            cmpidx += 1
            # Launch actual computation
            cmd_output = subprocess.Popen([
                "ssh",
                "-oStrictHostKeyChecking=no",
                cmp,
                "cd {cwd} \
                && touch {lockfile} \
                && {ffmpeg} -i \"{invid}\" -c:v libx264 -preset medium -crf {quality} \
                    -pix_fmt yuv420p -threads 0 -c:a copy -y \"{outvid}\" \
                && rm -f {lockfile}"\
                .format(
                    cwd=Path.cwd().as_posix(),
                    lockfile=(Path("./locks") / cmp.split('@')[-1]).as_posix(),
                    ffmpeg=FFMPEG,
                    invid=cloud.as_posix(),
                    quality=PASS_1_QUALITY,
                    outvid=out_file.as_posix()
                )
            ],
            stdout=sys.stdout, stderr=sys.stderr)
            # stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            # stdout=subprocess.PIPE, stderr=sys.stderr)
            print("[{}] encodage n°1 de {} lancé sur {}."\
                .format(datetime.datetime.now().strftime("%H:%M:%S"), cloud, cmp))
        else:
            print("[{}] Skipped  {}: {} exists."\
                .format(datetime.datetime.now().strftime("%H:%M:%S"), cloud, out_file))
    print(("====================================================\n"
         + "[{}] encodages n°1 (qualité constante) de {} lancés.\n"
         + "====================================================\n")\
           .format(datetime.datetime.now().strftime("%H:%M:%S"),
                   folder_to_compute))
    # Wait for encodings 1 to end
    sleep(4)
    n_remaining = len(list(locks_folder.glob('*')))
    while n_remaining > 0:
        print("[{}] {} encodings in progress, waiting     \b\b\b\b\b"\
              .format(datetime.datetime.now().strftime("%H:%M:%S"),
                      n_remaining),
              end='', flush=True)
        sleep(1)
        print('.', end='', flush=True)
        sleep(1)
        print('.', end='', flush=True)
        sleep(1)
        print('.', end='\r', flush=True)
        sleep(1)
        n_remaining = len(list(locks_folder.glob('*')))
    print(("\n====================================================\n"
         + "[{}] encodage n°1 (qualité constante) de {} terminé.\n"
         + "====================================================\n")\
           .format(datetime.datetime.now().strftime("%H:%M:%S"),
                   folder_to_compute))

    sleep(3)
    ##### Read encoding 1 sizes #####
    print("[{}] Lecture des tailles de fichiers"\
          .format(datetime.datetime.now().strftime("%H:%M:%S")))
    sum_sizes = 0
    for cloud in folder_to_compute.rglob('*.mp4'):
        print("[{}] lecture de {}"\
            .format(datetime.datetime.now().strftime("%H:%M:%S"), cloud))
        out_file = output_1_folder / cloud.relative_to(folder_to_compute)
        # Read coef
        coefpath = cloud.parent / '.coef'
        i = 0
        while not coefpath.is_file():
            coefpath = coefpath.parent.parent / '.coef'
            i += 1
            if i > 100:
                raise FileNotFoundError("Could not find .coef file for {}."\
                    .format(cloud))
        coef = int(coefpath.read_text().strip())
        # Check if the encoding 1 worked
        if (not out_file.is_file()):
            raise FileNotFoundError("Could not find output of first encoding for {}. Path searched: {}"\
                             .format(cloud, out_file))
        cmd_out = subprocess.run([
            FFPROBE,
            "-v",
            "error",
            "-show_entries",
            "format=size",
            "-of",
            "default=noprint_wrappers=1:nokey=1",
            out_file.as_posix()
        ], stdout=subprocess.PIPE, stderr=sys.stderr)
        sum_sizes += coef * int(cmd_out.stdout)
    
    print("[{}] Encodage n°2"\
          .format(datetime.datetime.now().strftime("%H:%M:%S"))) 
    ##### Do encoding 2 (two-pass actual encoding) #####
    # Make folders
    output_2_folder = folder_to_compute.parent / 'encoding_final_output'
    output_2_folder.mkdir(exist_ok=True)
    for cloud in folder_to_compute.rglob('*.mp4'):
        out_1_file = output_1_folder / cloud.relative_to(folder_to_compute)
        # Check if the encoding 1 worked
        if (not out_1_file.is_file()):
            raise ValueError("Could not find output of first encoding for {}"\
                             .format(out_file))
        # Create parent folders
        out_folder = output_2_folder / cloud.parent.relative_to(folder_to_compute)
        out_folder.mkdir(exist_ok=True, parents=True)
        # Get a computer
        cmp = computers[cmpidx % len(computers)]
        cmpidx += 1
        # Read coef
        coefpath = cloud.parent / '.coef'
        while not coefpath.is_file():
            coefpath = coefpath.parent.parent / '.coef'
        coef = int(coefpath.read_text().strip())
        # Calculate bitrate
        c_bitrate_cmd_out = subprocess.run([
            FFPROBE,
            "-v",
            "error",
            "-show_entries",
            "format=bit_rate",
            "-of",
            "default=noprint_wrappers=1:nokey=1",
            out_1_file
        ], stdout=subprocess.PIPE, stderr=sys.stderr)
        c_bitrate = int(c_bitrate_cmd_out.stdout)
        nice_bitrate = target_size * c_bitrate * coef / sum_sizes - AUDIO_BITRATE
        # Launch actual encoding
        cmd_output = subprocess.Popen([
            "ssh",
            "-oStrictHostKeyChecking=no",
            cmp,
            "cd {cwd} \
            && touch {lockfile} \
            && ./two_pass_one_file.sh \"{invid}\" \"{outvid}\" {v_bitrate} {a_bitrate}\
            && rm -f {lockfile}"\
            .format(
                cwd=Path.cwd().as_posix(),
                lockfile=(Path("./locks") / cmp.split('@')[-1]).as_posix(),
                invid=cloud.as_posix(),
                outvid=(out_folder / cloud.name).as_posix(),
                v_bitrate=nice_bitrate,
                a_bitrate=AUDIO_BITRATE
            )
        ],
        # stdout=sys.stdout, stderr=sys.stderr)
        # stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        stdout=subprocess.PIPE, stderr=sys.stderr)
        print("[{}] encodage n°2 de {} lancé sur {}."\
              .format(datetime.datetime.now().strftime("%H:%M:%S"), cloud, cmp))
    
    # Wait for encodings 2 to end
    sleep(2)
    n_remaining = len(list(locks_folder.glob('*')))
    while n_remaining > 0:
        print("[{}] {} encodings in progress, waiting   \b\b\b"\
              .format(datetime.datetime.now().strftime("%H:%M:%S"),
                      n_remaining),
              end='', flush=True)
        sleep(1)
        print('.', end='', flush=True)
        sleep(1)
        print('.', end='', flush=True)
        sleep(1)
        print('.', end='\r', flush=True)
        n_remaining = len(list(locks_folder.glob('*')))
    print(("\n====================================================\n"
         + "[{}] encodage n°2 (final)  de {} terminé.\n"
         + "====================================================\n")\
           .format(datetime.datetime.now().strftime("%H:%M:%S"),
                   folder_to_compute))

    # Clean
    locks_folder.rmdir()


if __name__ == '__main__':
    compute()
