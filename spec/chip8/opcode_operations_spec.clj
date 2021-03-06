(ns chip8.opcode-operations-spec
  (:require [speclj.core :refer :all]
            [chip8.cpu :as cpu]
            [chip8.opcode-operations :refer :all]))

(describe "chip8 opcode operations test"
  (it "should set a V register given a hex code"
      (let [cpu (cpu/build-cpu)
            cpu-vreg-set (-> cpu
                             (vreg-set "04" "a2")
                             (vreg-set "07" "f0"))]
        (should= 162 (cpu/vreg-get cpu-vreg-set 4))
        (should= 240 (cpu/vreg-get cpu-vreg-set 7))))

  (it "should get the value from a V register"
      (let [cpu (-> (cpu/build-cpu)
                    (vreg-set "04" "a2"))]
        (should= 162 (vreg-get cpu "04"))))

  (it "should return from a subroutine"
      (let [cpu (-> (cpu/build-cpu)
                    (cpu/stack-push 123)
                    (cpu/stack-push 456)
                    (cpu/sp-set 1))
            cpu-after-sub (return-from-subroutine cpu)]
        (should= 456 (:pc cpu-after-sub))
        (should= '(123) (:stack cpu-after-sub))
        (should= 0 (:sp cpu-after-sub))))

  (it "should jump to address"
      (let [cpu (cpu/build-cpu)
            cpu-after-jump (jump cpu "2f0")]
        (should= 752 (:pc cpu-after-jump))))

  (it "should call a subroutine"
      (let [cpu (-> (cpu/build-cpu)
                    (cpu/pc-set 123))
            cpu-call-sub (call-subroutine cpu "2f0")]
        (should= 1 (:sp cpu-call-sub))
        (should= '(123) (:stack cpu-call-sub))
        (should= 752 (:pc cpu-call-sub))))

  (it "should skip if the value at Vx is equal to the value passed in"
      (let [cpu (-> (cpu/build-cpu)
                    (cpu/vreg-set 4 752))
            cpu-skipped (skip-if-eq cpu 4 "2f0")]
        (should= 514 (:pc cpu-skipped))))

  (it "should not skip if the value at Vx is not equal to the value passed in"
      (let [cpu (-> (cpu/build-cpu)
                    (cpu/vreg-set 4 752))
            cpu-not-skipped (skip-if-eq cpu 4 "2f")]
        (should= 512 (:pc cpu-not-skipped))))

  (it "should skip if the value at Vx is not equal to the val passed in"
      (let [cpu (-> (cpu/build-cpu)
                    (cpu/vreg-set 4 752))
            cpu-skipped (skip-if-not-eq cpu 4 "2f")]
        (should= 514 (:pc cpu-skipped))))

  (it "should not skip if the value at Vx is equal to the val passed in"
      (let [cpu (-> (cpu/build-cpu)
                    (cpu/vreg-set 4 752))
            cpu-not-skipped (skip-if-not-eq cpu 4 "2f0")]
        (should= 512 (:pc cpu-not-skipped))))

  (it "should skip if the value in Vx and the value in Vy are equal"
      (let [cpu (-> (cpu/build-cpu)
                    (cpu/vreg-set 4 123)
                    (cpu/vreg-set 7 123))
            cpu-skipped (skip-if-vx-vy-eq cpu 4 7)]
        (should= 514 (:pc cpu-skipped))))

  (it "should not skip if Vx and Vy are not equal"
      (let [cpu (-> (cpu/build-cpu)
                    (cpu/vreg-set 4 123)
                    (cpu/vreg-set 7 456))
            cpu-not-skipped (skip-if-vx-vy-eq cpu 4 7)]
        (should= 512 (:pc cpu-not-skipped))))

  (it "should skip an instruction"
      (let [cpu (cpu/build-cpu)
            cpu-skipped (skip-instruction cpu)]
        (should= 514 (:pc cpu-skipped))))

  (it "should set the I register"
      (let [cpu (cpu/build-cpu)
            cpu-ireg-set (ireg-set cpu "a2")]
        (should= 162 (:Ireg cpu-ireg-set))))

  (it "should get the I register"
      (let [cpu (cpu/build-cpu)
            cpu-ireg-set (cpu/ireg-set cpu 162)]
        (should= 162 (ireg-get cpu-ireg-set))))

  (it "should set the delay timer"
      (let [cpu (cpu/build-cpu)
            cpu-delay-timer-set (set-delay-timer cpu "05")]
        (should= 5 (:delay-timer cpu-delay-timer-set))))

  (it "should set the sound timer"
      (let [cpu (cpu/build-cpu)
            cpu-sound-timer-set (set-sound-timer cpu "05")]
        (should= 5 (:sound-timer cpu-sound-timer-set))))

  (it "should get vxs"
      (let [cpu (-> (cpu/build-cpu)
                    (cpu/vreg-set 0 0)
                    (cpu/vreg-set 1 1)
                    (cpu/vreg-set 2 2))]
        (should= [0 1 2] (get-vxs cpu 2))))

  (it "should write multiple vx values into memory starting at the value in the I register"
      (let [cpu (-> (cpu/build-cpu)
                    (cpu/vreg-set 0 0)
                    (cpu/vreg-set 1 1)
                    (cpu/vreg-set 2 2)
                    (cpu/ireg-set 3))
            cpu-mem-inserted (write-vxs-to-mem cpu 3)]
        (should= [0 1 2] (subvec (:memory cpu-mem-inserted) 3 6))))
  
  (it "should write multiple values from memory into vxs starting at mem location ireg value"
      (let [cpu (-> (cpu/build-cpu)
                    (cpu/mem-insert 0 0)
                    (cpu/mem-insert 1 1)
                    (cpu/mem-insert 2 2))
            cpu-vxs-set (write-mem-to-vxs cpu 3)]
        (should= [0 1 2] (vreg-get cpu-vxs-set 0 2))))
  )
