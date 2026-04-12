package com.libratrack.api;

import com.libratrack.model.Fine;
import com.libratrack.service.FineService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.List;

public class FineController {
    private final FineService fineService;

    public FineController(FineService fineService) {
        this.fineService = fineService;
    }

    public void registerRoutes(Javalin app) {
        app.get("/api/fines/{memberId}", this::getUnpaidFines);
        app.get("/api/fines/{memberId}/total", this::getTotalUnpaid);
        app.post("/api/fines/{fineId}/pay", this::payFine);
    }

    private void getUnpaidFines(Context ctx) {
        int memberId = Integer.parseInt(ctx.pathParam("memberId"));
        ctx.json(fineService.getUnpaidFines(memberId));
    }

    private void getTotalUnpaid(Context ctx) {
        int memberId = Integer.parseInt(ctx.pathParam("memberId"));
        ctx.json(new TotalResponse(fineService.getTotalUnpaid(memberId)));
    }

    private void payFine(Context ctx) {
        int fineId = Integer.parseInt(ctx.pathParam("fineId"));
        PayRequest req = ctx.bodyAsClass(PayRequest.class);
        List<Fine> unpaidFines = fineService.getUnpaidFines(req.memberId());
        fineService.payFine(fineId, unpaidFines);
        ctx.json(new MessageResponse("Fine #" + fineId + " paid successfully."));
    }

    public record PayRequest(int memberId) {}
    public record TotalResponse(double total) {}
    public record MessageResponse(String message) {}
}
