package de.vonengel.g930beat;

import java.io.IOException;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Heartbeat {
    private static final Logger LOG = LoggerFactory.getLogger(Heartbeat.class);
    private Timer timer;
    private Clip clip;

    public void start(String mixerName) throws Exception {
        AudioInputStream ais = loadAudioInput();
        Mixer.Info mixerInfo = pickMixer(mixerName);
        this.clip = AudioSystem.getClip(mixerInfo);
        this.clip.open(ais);
        LOG.info("Starting heartbeat ...");
        logParameters(mixerInfo);
        TimerTask task = new TimerTask() {
            public void run() {
                Heartbeat.LOG.info("Sending heartbeat");
                Heartbeat.this.clip.setFramePosition(0);
                Heartbeat.this.clip.start();
            }
        };
        this.timer = new Timer("Heartbeat930", true);
        this.timer.scheduleAtFixedRate(task, 0L, computePeriod());
    }

    public Mixer.Info pickMixer(String desiredName) {
        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
            if (info.getName().equals(desiredName)) {
                return info;
            }
        }
        throw new NoSuchElementException(desiredName);
    }

    private void logParameters(Mixer.Info mixerInfo) {
        LOG.info("Mixer: {}", mixerInfo.getName());
        LOG.info("File: {}", HeartbeatProperties.getFile());
        LOG.info("Period: {}", Long.valueOf(HeartbeatProperties.getPeriod()));
    }

    public void stop() {
        this.timer.cancel();
        this.clip.close();
    }

    private AudioInputStream loadAudioInput() throws IOException,
            UnsupportedAudioFileException {
        URL url = super.getClass().getClassLoader()
                .getResource(HeartbeatProperties.getFile());
        AudioInputStream ais = AudioSystem.getAudioInputStream(url);
        return ais;
    }

    private long computePeriod() {
        return (HeartbeatProperties.getPeriod() * 1000L);
    }
}