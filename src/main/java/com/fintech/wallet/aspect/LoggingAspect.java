package com.fintech.wallet.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import java.util.Arrays;

@Aspect
@Component
@Slf4j // ใช้สำหรับเรียก log.info, log.error
public class LoggingAspect {

    // 1. กำหนด Pointcut: ดักจับทุก Method ใน WalletService
    @Pointcut("execution(* com.fintech.wallet.service.WalletService.*(..))")
    public void walletServiceMethods() {}

    // 2. ดักจับก่อนเริ่มทำงาน (Before)
    @Before("walletServiceMethods()")
    public void logBefore(JoinPoint joinPoint) {
        log.info("==> [AUDIT START] Method: {} | Args: {}", 
                joinPoint.getSignature().getName(), 
                Arrays.toString(joinPoint.getArgs()));
    }

    // 3. ดักจับเมื่อทำงานสำเร็จ (After Returning)
    @AfterReturning(pointcut = "walletServiceMethods()", returning = "result")
    public void logAfterSuccess(JoinPoint joinPoint, Object result) {
        log.info("<== [AUDIT SUCCESS] Method: {} | Result: {}", 
                joinPoint.getSignature().getName(), 
                result);
    }

    // 4. ดักจับเมื่อเกิดข้อผิดพลาด (After Throwing)
    @AfterThrowing(pointcut = "walletServiceMethods()", throwing = "exception")
    public void logAfterError(JoinPoint joinPoint, Exception exception) {
        log.error("!!! [AUDIT ERROR] Method: {} | Cause: {}", 
                joinPoint.getSignature().getName(), 
                exception.getMessage());
    }
}