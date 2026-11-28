package aq.project.mappers;

import aq.project.dto.DepositWalletRequestDto;
import aq.project.dto.TransferWalletRequestDto;
import aq.project.dto.WithdrawalWalletRequestDto;
import aq.project.entities.OperationType;
import aq.project.entities.WalletOperation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OperationMapper {

    OperationMapper INSTANCE = Mappers.getMapper(OperationMapper.class);

    @Mapping(target = "id", source = "operationId")
    @Mapping(target = "wallet", ignore = true)
    @Mapping(target = "targetWallet", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "operationType", expression = "java(toDepositOperation())")
    WalletOperation toWalletOperation(DepositWalletRequestDto depositWalletRequestDto);

    default OperationType toDepositOperation() {
        return OperationType.DEPOSIT;
    }

    @Mapping(target = "id", source = "operationId")
    @Mapping(target = "wallet", ignore = true)
    @Mapping(target = "targetWallet", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "operationType", expression = "java(toWithdrawalOperation())")
    WalletOperation toWalletOperation(WithdrawalWalletRequestDto withdrawalWalletRequestDto);

    default OperationType toWithdrawalOperation() {
        return OperationType.WITHDRAWAL;
    }

    @Mapping(target = "id", source = "operationId")
    @Mapping(target = "wallet", ignore = true)
    @Mapping(target = "targetWallet", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "operationType", expression = "java(toTransferOperation())")
    WalletOperation toWalletOperation(TransferWalletRequestDto transferWalletRequestDto);

    default OperationType toTransferOperation() {
        return OperationType.TRANSFER;
    }
}