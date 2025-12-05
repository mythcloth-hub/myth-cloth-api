package com.mesofi.mythclothapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.mesofi.mythclothapi.figurines.FigurineService;

@Component
public class StartupRunner {
  @Autowired private FigurineService figurineService;

  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    figurineService.importFromPublicDrive("1TKlCnCAp2o3hjT35kgtVEXpDxkAplIA8HBBPx-kqKio");
  }
}
